package com.example.techsmart.ui.auth

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.techsmart.data.repository.RegistroDatos

// Formato automático de DUI: ########-#
private fun formatearDui(texto: String): String {
    val digitos = texto.filter(Char::isDigit).take(9)
    return if (digitos.length > 8) "${digitos.take(8)}-${digitos.drop(8)}" else digitos
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    var tipo by rememberSaveable { mutableStateOf("empresarial") }
    var razonSocial by rememberSaveable { mutableStateOf("") }
    var nit by rememberSaveable { mutableStateOf("") }
    var nombre by rememberSaveable { mutableStateOf("") }
    var dui by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.success) {
        if (state.success) {
            vm.consumeSuccess()
            onRegisterSuccess()
        }
    }

    fun enviar() {
        val esEmpresa = tipo == "empresarial"
        when {
            esEmpresa && razonSocial.isBlank() -> vm.showError("Ingresa la razón social.")
            esEmpresa && nit.isBlank() -> vm.showError("Ingresa el NIT / NRC.")
            nombre.isBlank() -> vm.showError("Ingresa el nombre de contacto.")
            !esEmpresa && dui.isNotEmpty() && dui.length < 10 ->
                vm.showError("El DUI debe tener 9 dígitos (ej. 12345678-9).")
            telefono.isBlank() -> vm.showError("Ingresa un teléfono.")
            !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() ->
                vm.showError("Ingresa un correo válido.")
            password.length < 6 -> vm.showError("La contraseña debe tener al menos 6 caracteres.")
            password != confirm -> vm.showError("Las contraseñas no coinciden.")
            else -> vm.register(
                RegistroDatos(
                    tipoCuenta = tipo,
                    razonSocial = razonSocial.trim(),
                    nit = nit.trim(),
                    nombreContacto = nombre.trim(),
                    telefono = telefono.trim(),
                    correo = correo,
                    dui = dui
                ),
                password
            )
        }
    }

    @Composable
    fun campo(
        valor: String,
        alCambiar: (String) -> Unit,
        etiqueta: String,
        teclado: KeyboardType = KeyboardType.Text,
        esClave: Boolean = false
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            label = { Text(etiqueta) },
            singleLine = true,
            visualTransformation = if (esClave) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tipo == "empresarial",
                onClick = { tipo = "empresarial" },
                label = { Text("Empresarial") }
            )
            FilterChip(
                selected = tipo == "personal",
                onClick = { tipo = "personal" },
                label = { Text("Personal") }
            )
        }
        Spacer(Modifier.height(12.dp))

        if (tipo == "empresarial") {
            campo(razonSocial, { razonSocial = it }, "Razón social / Nombre de la empresa")
            campo(nit, { nit = it }, "NIT / NRC (fiscal)")
            campo(nombre, { nombre = it }, "Contacto comercial")
        } else {
            campo(nombre, { nombre = it }, "Nombre completo")
            campo(dui, { dui = formatearDui(it) }, "DUI (opcional, 9 dígitos)", KeyboardType.Number)
        }

        campo(telefono, { telefono = it }, "Celular / WhatsApp", KeyboardType.Phone)
        campo(
            correo, { correo = it },
            if (tipo == "empresarial") "Correo corporativo" else "Correo electrónico personal",
            KeyboardType.Email
        )
        campo(password, { password = it }, "Contraseña (mín. 6 caracteres)", KeyboardType.Password, esClave = true)
        campo(confirm, { confirm = it }, "Confirmar contraseña", KeyboardType.Password, esClave = true)

        state.error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = ::enviar,
            enabled = !state.loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (state.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    if (tipo == "empresarial") "Crear cuenta empresarial"
                    else "Crear cuenta personal"
                )
            }
        }

        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}