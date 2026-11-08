package com.example.techsmart.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.techsmart.data.repository.AuthRepository
import com.example.techsmart.data.repository.RegistroDatos
import com.example.techsmart.data.repository.toFriendlyMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val success: Boolean = false
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(email: String, password: String) = ejecutar {
        repo.login(email, password)
    }

    fun register(datos: RegistroDatos, password: String) = ejecutar {
        repo.register(datos, password)
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _state.value = AuthUiState(error = "Escribe tu correo para recuperar la contraseña.")
            return
        }
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            repo.resetPassword(email)
                .onSuccess {
                    _state.value = AuthUiState(info = "Te enviamos un correo para restablecerla.")
                }
                .onFailure { _state.value = AuthUiState(error = it.toFriendlyMessage()) }
        }
    }

    fun showError(message: String) {
        _state.value = AuthUiState(error = message)
    }

    fun consumeSuccess() {
        _state.value = AuthUiState()
    }

    private fun ejecutar(bloque: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _state.value = AuthUiState(loading = true)
            bloque()
                .onSuccess { _state.value = AuthUiState(success = true) }
                .onFailure { _state.value = AuthUiState(error = it.toFriendlyMessage()) }
        }
    }
}