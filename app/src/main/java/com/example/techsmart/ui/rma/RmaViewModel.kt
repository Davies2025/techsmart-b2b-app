package com.example.techsmart.ui.rma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techsmart.data.model.TicketRMA
import com.example.techsmart.data.repository.TechsmartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RmaUiState(
    val isSubmitting: Boolean = false,
    val ticketCreadoId: String? = null,
    val error: String? = null
)

class RmaViewModel(
    private val repository: TechsmartRepository = TechsmartRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RmaUiState())
    val uiState: StateFlow<RmaUiState> = _uiState.asStateFlow()

    fun crearTicketRMA(
        sku: String,
        numeroSerie: String,
        factura: String,
        cliente: String,
        descripcionFalla: String,
        esDOA: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val ticket = TicketRMA(
                sku = sku,
                numeroSerie = numeroSerie,
                facturaReferencia = factura,
                cliente = cliente,
                descripcionFalla = descripcionFalla,
                esDOA = esDOA
            )

            val result = repository.crearTicketRMA(ticket)
            result.fold(
                onSuccess = { id ->
                    _uiState.update { it.copy(isSubmitting = false, ticketCreadoId = id) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isSubmitting = false, error = err.localizedMessage) }
                }
            )
        }
    }
}