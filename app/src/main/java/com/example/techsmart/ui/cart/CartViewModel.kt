package com.example.techsmart.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.ItemCotizacion
import com.example.techsmart.data.model.Pago
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

/** Datos de una compra ("Comprar ahora"), ya validados por el checkout. */
data class DatosCompra(
    val tipoCliente: String,
    val razonSocial: String,
    val documentoFiscal: String,   // NIT (empresa) o DUI / "Consumidor Final" (persona)
    val contacto: String,
    val telefono: String,
    val correo: String,
    val metodoPago: String,        // Cotizacion.PAGO_*
    val pagoReferencia: String     // "Visa •••• 1111", "Transferencia bancaria", "Crédito Techsmart 30 días"
)

data class CartUiState(
    val items: List<ItemCotizacion> = emptyList(),
    // Producto completo de cada partida, para recalcular el precio por volumen al cambiar la cantidad
    val productos: Map<String, Producto> = emptyMap(),
    val totales: TotalesCotizacion = TotalesCotizacion(),
    val perfil: Usuario? = null,
    val misCotizaciones: List<Cotizacion> = emptyList(),
    val cargandoCotizaciones: Boolean = false,
    val misPagos: List<Pago> = emptyList(),
    val isSubmitting: Boolean = false,
    val folioGuardado: String? = null,
    val compraConfirmada: String? = null,   // folio de la compra recién pagada
    val error: String? = null
) {
    /** Línea de crédito del perfil (el saldo no se descuenta: es una validación simulada). */
    val lineaCredito: Double get() = perfil?.lineaCredito?.toDouble() ?: 0.0
}

class CartViewModel(
    private val repository: TechsmartRepository = TechsmartRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private var jobCotizaciones: Job? = null
    private var jobPagos: Job? = null

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
    // Escucha en tiempo real los pagos de la cuenta (historial y pantalla del pedido)
    fun observarMisPagos() {
        jobPagos?.cancel()
        jobPagos = viewModelScope.launch {
            repository.getMisPagos().collect { result ->
                result.fold(
                    onSuccess = { lista -> _uiState.update { it.copy(misPagos = lista) } },
                    onFailure = { err -> _uiState.update { it.copy(error = err.toFriendlyMessage()) } }
                )
            }
        }
    }

    fun vaciar() {
        jobCotizaciones?.cancel()
        jobCotizaciones = null
        jobPagos?.cancel()
        jobPagos = null
        _uiState.value = CartUiState()
    }

    // El propietario de la cotización (usuarioCorreo), la fecha y el folio los asigna el repositorio.
    fun guardarCotizacion(datos: DatosCotizacion) {
        val estado = _uiState.value
        if (estado.items.isEmpty() || estado.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val cotizacion = Cotizacion(
                estado = Cotizacion.ESTADO_EN_REVISION,
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

    // -----------------------------------------------------------------
    // Compra ("Comprar ahora" -> carrito -> checkout). Pago SIMULADO.
    // -----------------------------------------------------------------

    /**
     * Registra la compra en 'cotizaciones' (igual que el checkout de la web):
     * - Tarjeta o crédito: queda "Aprobada" (pagada).
     * - Transferencia: queda "Pendiente de Pago" hasta que un administrador la confirme.
     * Los avances siguientes (En Bodega, Despachada, Entregada) los hace un administrador desde la web.
     */
    fun comprar(datos: DatosCompra) {
        val estado = _uiState.value
        if (estado.items.isEmpty() || estado.isSubmitting) return

        // Revalida el crédito aquí por si el lote cambió después de elegir el método
        if (datos.metodoPago == Cotizacion.PAGO_CREDITO && estado.totales.total > estado.lineaCredito) {
            _uiState.update { it.copy(error = "El total supera tu línea de crédito disponible.") }
            return
        }

        val esTransferencia = datos.metodoPago == Cotizacion.PAGO_TRANSFERENCIA
        val esEmpresa = datos.tipoCliente == Cotizacion.TIPO_EMPRESA

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val compra = Cotizacion(
                folio = "B2B-${(1000..9999).random()}", // mismo formato que el checkout web
                estado = if (esTransferencia) Cotizacion.ESTADO_PENDIENTE_PAGO else Cotizacion.ESTADO_APROBADA,
                tipoCliente = datos.tipoCliente,
                razonSocial = datos.razonSocial.trim(),
                nit = datos.documentoFiscal.trim().ifBlank { "Consumidor Final" },
                contacto = datos.contacto.trim(),
                telefono = datos.telefono.trim(),
                correo = datos.correo.trim(),
                tiempoEntrega = "24 - 72 hrs",
                // Transferencia: 3 días para pagar. Pagada: no vence.
                validezDias = if (esTransferencia) 3 else 0,
                vencimiento = if (esTransferencia) "" else "Pagada",
                items = estado.items,
                subtotalNeto = estado.totales.subtotalNeto,
                descuento = estado.totales.descuento,
                iva = estado.totales.iva,
                total = estado.totales.total,
                notas = "Método de procesamiento: ${datos.metodoPago.uppercase()}",
                metodoPago = datos.metodoPago,
                pagoReferencia = datos.pagoReferencia,
                documentoFiscal = if (esEmpresa) Cotizacion.DOC_CCF else Cotizacion.DOC_CONSUMIDOR_FINAL
            )

            // Comprobante del pago (se guarda en 'pagos' junto con el pedido)
            val pago = Pago(
                metodo = datos.metodoPago,
                referencia = datos.pagoReferencia,
                monto = estado.totales.total,
                iva = estado.totales.iva,
                estado = if (esTransferencia) Pago.ESTADO_PENDIENTE else Pago.ESTADO_APROBADO,
                hora = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
            )

            repository.registrarCompra(compra, pago).fold(
                onSuccess = { folio ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            compraConfirmada = folio,
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

    // La pantalla de confirmación ya navegó: se limpia para no volver a navegar
    fun consumirCompraConfirmada() {
        _uiState.update { it.copy(compraConfirmada = null) }
    }
}
