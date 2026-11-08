package com.example.techsmart.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.ItemCotizacion
import com.example.techsmart.data.model.Producto
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.data.repository.TechsmartRepository
import com.example.techsmart.data.repository.toFriendlyMessage
import com.example.techsmart.domain.engine.TieredPricingEngine
import com.example.techsmart.domain.engine.TotalesCotizacion
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Datos que el cliente llena (o revisa) antes de enviar la cotización. */
data class DatosCotizacion(
    val tipoCliente: String = Cotizacion.TIPO_EMPRESA,
    val razonSocial: String = "",
    val nit: String = "",
    val contacto: String = "",
    val telefono: String = "",
    val correo: String = "",
    val tiempoEntrega: String = "24 - 72 hrs",
    val validezDias: Int = 7,
    val notas: String = ""
)

data class CartUiState(
    val items: List<ItemCotizacion> = emptyList(),
    // Producto completo de cada partida, para recalcular el precio por volumen al cambiar la cantidad
    val productos: Map<String, Producto> = emptyMap(),
    val totales: TotalesCotizacion = TotalesCotizacion(),
    val perfil: Usuario? = null,
    val misCotizaciones: List<Cotizacion> = emptyList(),
    val cargandoCotizaciones: Boolean = false,
    val isSubmitting: Boolean = false,
    val folioGuardado: String? = null,
    val error: String? = null
)

class CartViewModel(
    private val repository: TechsmartRepository = TechsmartRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private var jobCotizaciones: Job? = null

    fun agregarProducto(producto: Producto, cantidad: Int) {
        if (cantidad <= 0) return
        val actual = _uiState.value.items.find { it.productoId == producto.id }?.cantidad ?: 0
        fijarCantidad(producto, actual + cantidad)
    }

    fun cambiarCantidad(productoId: String, cantidad: Int) {
        val producto = _uiState.value.productos[productoId] ?: return
        if (cantidad <= 0) eliminarItem(productoId) else fijarCantidad(producto, cantidad)
    }

    fun eliminarItem(productoId: String) {
        _uiState.update {
            val items = it.items.filterNot { item -> item.productoId == productoId }
            it.copy(
                items = items,
                productos = it.productos - productoId,
                totales = TieredPricingEngine.calcularTotales(items)
            )
        }
    }

    private fun fijarCantidad(producto: Producto, cantidad: Int) {
        val nuevoItem = TieredPricingEngine.procesarPartida(producto, cantidad)
        _uiState.update {
            // Mantiene el orden original de las partidas
            val existe = it.items.any { item -> item.productoId == producto.id }
            val items = if (existe) {
                it.items.map { item -> if (item.productoId == producto.id) nuevoItem else item }
            } else {
                it.items + nuevoItem
            }
            it.copy(
                items = items,
                productos = it.productos + (producto.id to producto),
                totales = TieredPricingEngine.calcularTotales(items)
            )
        }
    }

    // Perfil de la cuenta, para precargar los datos del cliente en el formulario
    fun cargarPerfil() {
        viewModelScope.launch {
            repository.obtenerMiPerfil().onSuccess { perfil ->
                _uiState.update { it.copy(perfil = perfil) }
            }
        }
    }

    // Escucha en tiempo real las cotizaciones de la cuenta con sesión iniciada.
    // La pantalla lo llama al abrirse (la sesión puede iniciarse después de crear el ViewModel).
    fun observarMisCotizaciones() {
        jobCotizaciones?.cancel()
        _uiState.update { it.copy(cargandoCotizaciones = true) }
        jobCotizaciones = viewModelScope.launch {
            repository.getMisCotizaciones().collect { result ->
                result.fold(
                    onSuccess = { lista ->
                        _uiState.update {
                            it.copy(misCotizaciones = lista, cargandoCotizaciones = false)
                        }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(cargandoCotizaciones = false, error = err.toFriendlyMessage())
                        }
                    }
                )
            }
        }
    }

    // Quita el mensaje de "cotización guardada" una vez que la pantalla ya lo mostró
    fun consumirCotizacionGuardada() {
        _uiState.update { it.copy(folioGuardado = null) }
    }

    fun consumirError() {
        _uiState.update { it.copy(error = null) }
    }

    // Vacía todo el estado (se usa al cerrar sesión para que el carrito
    // no pase a la siguiente persona que use el dispositivo)
    fun vaciar() {
        jobCotizaciones?.cancel()
        jobCotizaciones = null
        _uiState.value = CartUiState()
    }

    // El propietario de la cotización (usuarioCorreo), la fecha y el folio los asigna el repositorio.
    fun guardarCotizacion(datos: DatosCotizacion) {
        val estado = _uiState.value
        if (estado.items.isEmpty() || estado.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val cotizacion = Cotizacion(
                estado = "En Revisión",
                tipoCliente = datos.tipoCliente,
                razonSocial = datos.razonSocial.trim(),
                nit = datos.nit.trim(),
                contacto = datos.contacto.trim(),
                telefono = datos.telefono.trim(),
                correo = datos.correo.trim(),
                tiempoEntrega = datos.tiempoEntrega,
                validezDias = datos.validezDias,
                items = estado.items,
                subtotalNeto = estado.totales.subtotalNeto,
                descuento = estado.totales.descuento,
                iva = estado.totales.iva,
                total = estado.totales.total,
                notas = datos.notas.trim()
            )

            repository.guardarCotizacion(cotizacion).fold(
                onSuccess = { folio ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            folioGuardado = folio,
                            items = emptyList(),
                            productos = emptyMap(),
                            totales = TotalesCotizacion()
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isSubmitting = false, error = err.toFriendlyMessage())
                    }
                }
            )
        }
    }
}
