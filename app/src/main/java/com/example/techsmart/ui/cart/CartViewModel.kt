package com.example.techsmart.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.ItemCotizacion
import com.example.techsmart.data.model.Producto
import com.example.techsmart.data.repository.TechsmartRepository
import com.example.techsmart.domain.engine.TieredPricingEngine
import com.example.techsmart.domain.engine.TotalesCotizacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<ItemCotizacion> = emptyList(),
    val totales: TotalesCotizacion = TotalesCotizacion(0.0, 0.0, 0.0),
    val isSubmitting: Boolean = false,
    val cotizacionGuardadaId: String? = null,
    val error: String? = null
)

class CartViewModel(
    private val repository: TechsmartRepository = TechsmartRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun agregarProducto(producto: Producto, cantidad: Int) {
        val itemExistente = _uiState.value.items.find { it.productoId == producto.id }
        val nuevaCantidad = (itemExistente?.cantidad ?: 0) + cantidad
        val nuevoItem = TieredPricingEngine.procesarPartida(producto, nuevaCantidad)

        val itemsActualizados = _uiState.value.items.filterNot { it.productoId == producto.id } + nuevoItem
        actualizarItems(itemsActualizados)
    }

    fun eliminarItem(productoId: String) {
        val itemsActualizados = _uiState.value.items.filterNot { it.productoId == productoId }
        actualizarItems(itemsActualizados)
    }

    private fun actualizarItems(nuevosItems: List<ItemCotizacion>) {
        val nuevosTotales = TieredPricingEngine.calcularTotales(nuevosItems)
        _uiState.update {
            it.copy(
                items = nuevosItems,
                totales = nuevosTotales
            )
        }
    }

    fun guardarCotizacion(
        razonSocial: String,
        nit: String,
        contacto: String,
        correo: String
    ) {
        if (_uiState.value.items.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val cotizacion = Cotizacion(
                razonSocial = razonSocial,
                nit = nit,
                contacto = contacto,
                correo = correo,
                items = _uiState.value.items,
                subtotalNeto = _uiState.value.totales.subtotalNeto,
                iva = _uiState.value.totales.iva,
                total = _uiState.value.totales.total
            )

            val result = repository.guardarCotizacion(cotizacion)
            result.fold(
                onSuccess = { id ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            cotizacionGuardadaId = id,
                            items = emptyList(),
                            totales = TotalesCotizacion(0.0, 0.0, 0.0)
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isSubmitting = false, error = err.localizedMessage)
                    }
                }
            )
        }
    }
}