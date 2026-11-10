package com.example.techsmart.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techsmart.data.repository.ChatRepository
import com.example.techsmart.data.repository.MensajeApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Mensaje del chat (mismo modelo que pide el documento de requerimientos). */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String,      // "user" | "bot"
    val text: String,
    val timestamp: String,   // ej. "10:45 AM"
    val esBienvenida: Boolean = false
)

data class ChatUiState(
    val mensajes: List<ChatMessage> = listOf(bienvenida()),
    val escribiendo: Boolean = false
)

const val MENSAJE_BIENVENIDA =
    "¡Hola! Bienvenido a Techsmart B2B El Salvador. ¿En qué componentes, precios por volumen o garantías puedo ayudarte hoy?"

const val MENSAJE_NO_DISPONIBLE =
    "Lo sentimos, el asistente no está disponible en este momento. Intenta más tarde."

// Cuántos mensajes se envían como contexto (los más recientes)
private const val MAX_HISTORIAL = 20

private fun horaActual(): String = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

private fun bienvenida() = ChatMessage(
    sender = "bot",
    text = MENSAJE_BIENVENIDA,
    timestamp = horaActual(),
    esBienvenida = true
)

/**
 * Estado del Asesor IA. Vive al nivel de la navegación para que la conversación
 * se conserve al cambiar de vista o al cerrar y abrir el chat.
 */
class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var peticion: Job? = null

    fun enviar(texto: String) {
        val limpio = texto.trim()
        if (limpio.isEmpty() || _uiState.value.escribiendo) return

        val mensajeUsuario = ChatMessage(sender = "user", text = limpio, timestamp = horaActual())
        _uiState.update { it.copy(mensajes = it.mensajes + mensajeUsuario, escribiendo = true) }

        // El saludo es local: no se envía como contexto
        val historial = _uiState.value.mensajes
            .filterNot { it.esBienvenida }
            .takeLast(MAX_HISTORIAL)
            .map { MensajeApi(sender = it.sender, text = it.text) }

        peticion = viewModelScope.launch {
            val respuesta = repository.enviar(historial).getOrElse { MENSAJE_NO_DISPONIBLE }
            _uiState.update {
                it.copy(
                    mensajes = it.mensajes + ChatMessage(sender = "bot", text = respuesta, timestamp = horaActual()),
                    escribiendo = false
                )
            }
        }
    }

    /** Empieza una conversación nueva (también se usa al cerrar sesión). */
    fun reiniciar() {
        peticion?.cancel()
        peticion = null
        _uiState.value = ChatUiState()
    }
}
