package com.example.techsmart.ui.auth

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.techsmart.R
import com.example.techsmart.data.repository.RegistroDatos

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

    var aceptaTerminos by rememberSaveable { mutableStateOf(false) }
    var mostrarDialogoTerminos by rememberSaveable { mutableStateOf(false) }

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
            telefono.length < 8 -> vm.showError("Ingresa un teléfono válido de 8 dígitos.")
            !Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() ->
                vm.showError("Ingresa un correo válido.")
            password.length < 6 -> vm.showError("La contraseña debe tener al menos 6 caracteres.")
            password != confirm -> vm.showError("Las contraseñas no coinciden.")
            !aceptaTerminos -> vm.showError("Debes aceptar los Términos y Condiciones para registrarte.")
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

    val primaryColor = MaterialTheme.colorScheme.primary
    val darkTechBlue = Color(0xFF00227B)
    val cyanAccent = Color(0xFF00C2FF)

    @Composable
    fun campo(
        valor: String,
        alCambiar: (String) -> Unit,
        etiqueta: String,
        teclado: KeyboardType = KeyboardType.Text,
        esClave: Boolean = false,
        icono: ImageVector? = null
    ) {
        var verClave by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = valor,
            onValueChange = alCambiar,
            label = {
                Text(
                    text = etiqueta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            leadingIcon = icono?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = primaryColor
                    )
                }
            },
            trailingIcon = if (esClave) {
                {
                    IconButton(onClick = { verClave = !verClave }) {
                        Icon(
                            imageVector = if (verClave) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = "Mostrar u ocultar contraseña"
                        )
                    }
                }
            } else null,
            visualTransformation = if (esClave && !verClave) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = teclado),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
    }

    @Composable
    fun EncabezadoSeccion(numero: String, titulo: String) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Surface(
                color = primaryColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = numero,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

    if (mostrarDialogoTerminos) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoTerminos = false },
            shape = RoundedCornerShape(22.dp),
            title = {
                Column {
                    Text(
                        text = "Términos y Condiciones",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                    Text(
                        text = "Plataforma Techsmart B2B & Express",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Registro y Veracidad de Datos:\n" +
                                "El usuario declara que la información proporcionada (Razón Social, NIT/NRC en cuentas empresariales o DUI en cuentas personales) es verídica y comprobable.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. Línea de Crédito Inicial:\n" +
                                "Techsmart asigna un cupo referencial inicial de $25,000 USD para cuentas Empresariales (B2B) y $3,500 USD para cuentas Personales (Express), sujeto a validación de compras y políticas de pago.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. Cotizaciones y Precios por Volumen:\n" +
                                "Las cotizaciones generadas en la aplicación están sujetas a disponibilidad de inventario OEM y vigencia de precios mayoristas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "4. Garantías y Trámites RMA:\n" +
                                "Todo reclamo de garantía (RMA) deberá registrarse mediante el módulo oficial de la plataforma adjuntando el número de serie o factura correspondiente.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "5. Privacidad y Seguridad:\n" +
                                "Sus credenciales están protegidas mediante cifrado y sus datos comerciales se emplean exclusivamente para operaciones dentro de Techsmart.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        aceptaTerminos = true
                        mostrarDialogoTerminos = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Aceptar y continuar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoTerminos = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(darkTechBlue, primaryColor)
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        onClick = onBackToLogin,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Volver al login",
                                tint = Color.White
                            )
                        }
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, cyanAccent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "REGISTRO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = "Imagotipo Techsmart",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(1.45f)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Crear cuenta",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = "Configura tu perfil comercial o personal en Techsmart",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(Modifier.height(16.dp))

                Surface(
                    color = Color.Black.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val esEmp = tipo == "empresarial"

                        Surface(
                            onClick = { tipo = "empresarial" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (esEmp) Color.White else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Empresarial (B2B)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esEmp) darkTechBlue else Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        Surface(
                            onClick = { tipo = "personal" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (!esEmp) Color.White else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Personal (Express)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!esEmp) darkTechBlue else Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (tipo == "empresarial")
                                    "LÍNEA DE CRÉDITO CORPORATIVA"
                                else
                                    "LÍNEA DE CRÉDITO EXPRESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = cyanAccent,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (tipo == "empresarial")
                                    "Precios mayoristas y facturación fiscal"
                                else
                                    "Compras rápidas con DUI o Consumidor Final",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        Text(
                            text = if (tipo == "empresarial") "$25,000" else "$3,500",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EncabezadoSeccion(
                        numero = "01",
                        titulo = if (tipo == "empresarial") "Datos de la Empresa" else "Datos Personales"
                    )

                    if (tipo == "empresarial") {
                        campo(
                            razonSocial,
                            { razonSocial = it },
                            "Razón social / Empresa",
                            icono = Icons.Filled.AccountBox
                        )
                        campo(
                            nit,
                            { nit = it },
                            "NIT / NRC (fiscal)",
                            icono = Icons.Filled.Info
                        )
                        campo(
                            nombre,
                            { nombre = it },
                            "Contacto comercial",
                            icono = Icons.Filled.Person
                        )
                    } else {
                        campo(
                            nombre,
                            { nombre = it },
                            "Nombre completo",
                            icono = Icons.Filled.Person
                        )
                        campo(
                            dui,
                            { dui = formatearDui(it) },
                            "DUI (opcional, 9 dígitos)",
                            KeyboardType.Number,
                            icono = Icons.Filled.Info
                        )
                    }
                }
            }

            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EncabezadoSeccion(numero = "02", titulo = "Contacto y Acceso")

                    campo(
                        telefono,
                        { telefono = it.filter(Char::isDigit).take(8) },
                        "Celular / WhatsApp (8 dígitos)",
                        KeyboardType.Number,
                        icono = Icons.Filled.Phone
                    )
                    campo(
                        correo,
                        { correo = it },
                        if (tipo == "empresarial") "Correo corporativo" else "Correo electrónico personal",
                        KeyboardType.Email,
                        icono = Icons.Filled.Email
                    )
                }
            }

            ElevatedCard(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    EncabezadoSeccion(numero = "03", titulo = "Seguridad y Términos")

                    campo(
                        password,
                        { password = it },
                        "Contraseña (mín. 6 caracteres)",
                        KeyboardType.Password,
                        esClave = true,
                        icono = Icons.Filled.Lock
                    )
                    campo(
                        confirm,
                        { confirm = it },
                        "Confirmar contraseña",
                        KeyboardType.Password,
                        esClave = true,
                        icono = Icons.Filled.Lock
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (aceptaTerminos) primaryColor.copy(alpha = 0.4f)
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Checkbox(
                                checked = aceptaTerminos,
                                onCheckedChange = { aceptaTerminos = it }
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "He leído y acepto los ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Términos y Condiciones de Techsmart",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable { mostrarDialogoTerminos = true }
                                )
                            }
                        }
                    }
                }
            }

            state.error?.let {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = ::enviar,
                enabled = !state.loading,
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (state.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (tipo == "empresarial") "Crear cuenta empresarial"
                        else "Crear cuenta personal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            TextButton(
                onClick = onBackToLogin,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = "¿Ya tienes cuenta? Inicia sesión aquí",
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}