package com.example.techsmart.ui.rma

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techsmart.data.model.TicketRMA
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.data.repository.TechsmartRepository
import com.example.techsmart.data.repository.toFriendlyMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Datos del formulario de reclamo. */
data class DatosReclamo(
    val productoNombre: String = "",
    val sku: String = "",
    val numeroSerie: String = "",
    val facturaReferencia: String = "",
    val cliente: String = "",
    val contacto: String = "",
    val telefono: String = "",
    val correo: String = "",
    val descripcionFalla: String = "",
    val esDOA: Boolean = false
)

data class RmaUiState(
    val tickets: List<TicketRMA> = emptyList(),
    val cargandoTickets: Boolean = false,
    val perfil: Usuario? = null,
    val isSubmitting: Boolean = false,
    val folioCreado: String? = null,
    val error: String? = null
)

class RmaViewModel(
    private val repository: TechsmartRepository = TechsmartRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RmaUiState())
    val uiState: StateFlow<RmaUiState> = _uiState.asStateFlow()

    private var jobTickets: Job? = null

    // Escucha en tiempo real los reclamos de la cuenta con sesión iniciada.
    // La pantalla debe llamarlo al abrirse (la sesión puede iniciarse después de crear el ViewModel).
    fun observarMisTickets() {
        jobTickets?.cancel()
        _uiState.update { it.copy(cargandoTickets = true) }
        jobTickets = viewModelScope.launch {
            repository.getMisTicketsRMA().collect { result ->
                result.fold(
                    onSuccess = { lista ->
                        _uiState.update { it.copy(tickets = lista, cargandoTickets = false) }
                    },
                    onFailure = { err ->
                        _uiState.update {
                            it.copy(cargandoTickets = false, error = err.toFriendlyMessage())
                        }
                    }
                )
            }
        }
    }

    // Perfil de la cuenta, para precargar los datos de contacto del reclamo
    fun cargarPerfil() {
        viewModelScope.launch {
            repository.obtenerMiPerfil().onSuccess { perfil ->
                _uiState.update { it.copy(perfil = perfil) }
            }
        }
    }

    // Quita el mensaje de "ticket creado" una vez que la pantalla ya lo mostró
    fun consumirTicketCreado() {
        _uiState.update { it.copy(folioCreado = null) }
    }

    fun consumirError() {
        _uiState.update { it.copy(error = null) }
    }

    // Detiene la escucha y vacía el estado (se usa al cerrar sesión)
    fun vaciar() {
        jobTickets?.cancel()
        jobTickets = null
        _uiState.value = RmaUiState()
    }

    // El propietario (usuarioCorreo), el folio, la fecha y el estado inicial
    // ("Cambio Aprobado" si es DOA, si no "Reportado") los asigna el repositorio.
    fun crearTicketRMA(datos: DatosReclamo) {
        if (_uiState.value.isSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val ticket = TicketRMA(
                productoNombre = datos.productoNombre.trim(),
                sku = datos.sku.trim(),
                numeroSerie = datos.numeroSerie.trim(),
                facturaReferencia = datos.facturaReferencia.trim(),
                cliente = datos.cliente.trim(),
                contacto = datos.contacto.trim(),
                telefono = datos.telefono.trim(),
                correo = datos.correo.trim(),
                descripcionFalla = datos.descripcionFalla.trim(),
                esDOA = datos.esDOA,
                diagnosticoTecnico = if (datos.esDOA) {
                    TicketRMA.DIAGNOSTICO_DOA
                } else {
                    TicketRMA.DIAGNOSTICO_NORMAL
                }
            )

            repository.crearTicketRMA(ticket).fold(
                onSuccess = { folio ->
                    _uiState.update { it.copy(isSubmitting = false, folioCreado = folio) }
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
