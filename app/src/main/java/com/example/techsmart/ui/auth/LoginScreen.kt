package com.example.techsmart.ui.auth

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.techsmart.R
import com.example.techsmart.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    onRecuperarPasswordClick: () -> Unit = {},
    vm: AuthViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        if (state.success) {
            vm.consumeSuccess()
            onLoginSuccess()
        }
    }

    val azulNoche = Color(0xFF0A1931)
    val azulReal = Color(0xFF155EEF)
    val cianPulso = Color(0xFF00D2FF)
    val verdeEsmeralda = Color(0xFF0A876C)
    val verdeMenta = Color(0xFF34D399)
    val formListo = email.isNotBlank() && password.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEAF1FF),
                        TsFondo,
                        Color(0xFFE8F5F1)
                    )
                )
            )
    ) {
        // Fondo arquitectónico Precision Pulse
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val step = 28.dp.toPx()
            val dotRadius = 1.2.dp.toPx()
            val dotColor = TsPrimario.copy(alpha = 0.06f)

            var x = step / 2f
            while (x < w) {
                var y = step / 2f
                while (y < h) {
                    drawCircle(
                        color = dotColor,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    y += step
                }
                x += step
            }

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(azulReal.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(w * 0.90f, h * 0.08f),
                    radius = w * 0.55f
                ),
                center = Offset(w * 0.90f, h * 0.08f),
                radius = w * 0.55f
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(verdeEsmeralda.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(w * 0.10f, h * 0.92f),
                    radius = w * 0.55f
                ),
                center = Offset(w * 0.10f, h * 0.92f),
                radius = w * 0.55f
            )

            val stroke = 2.dp.toPx()
            drawLine(
                color = azulReal.copy(alpha = 0.15f),
                start = Offset(0f, h * 0.93f),
                end = Offset(w * 0.22f, h * 0.93f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = azulReal.copy(alpha = 0.15f),
                start = Offset(w * 0.22f, h * 0.93f),
                end = Offset(w * 0.28f, h * 0.90f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = verdeEsmeralda.copy(alpha = 0.35f),
                radius = 4.dp.toPx(),
                center = Offset(w * 0.28f, h * 0.90f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- HERO BANNER EJECUTIVO ----------
            Surface(
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 8.dp,
                border = BorderStroke(1.2.dp, cianPulso.copy(alpha = 0.38f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(azulNoche, Color(0xFF0F2C67), azulReal)
                            )
                        )
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val cx = size.width * 0.88f
                        val cy = size.height * 0.22f
                        drawCircle(
                            color = cianPulso.copy(alpha = 0.14f),
                            radius = size.width * 0.32f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = size.width * 0.20f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Imagotipo con marco luminoso
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                cianPulso.copy(alpha = 0.30f),
                                                verdeMenta.copy(alpha = 0.25f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .padding(4.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(
                                            width = 1.dp,
                                            color = cianPulso.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
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

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(verdeMenta)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "PORTAL B2B & EXPRESS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FuenteMono,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.8.sp,
                                            color = cianPulso
                                        )
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = "Techsmart",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Hardware, Redes y Suministros TI",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.82f)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Barra de garantías corporativas integrada en el Hero
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Black.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SelloHero("Stock OEM", verdeMenta)
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(12.dp)
                                        .background(Color.White.copy(alpha = 0.2f))
                                )
                                SelloHero("Precios Volumen", cianPulso)
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(12.dp)
                                        .background(Color.White.copy(alpha = 0.2f))
                                )
                                SelloHero("Crédito", verdeMenta)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Surface(
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.18f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(azulReal, cianPulso, verdeEsmeralda)
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 22.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(22.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(azulReal, cianPulso)
                                            )
                                        )
                                )
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Acceso al Portal",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TsSecundario
                                    )
                                    Text(
                                        text = "Ingresa con tu cuenta empresarial o personal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TsNeutro
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = verdeEsmeralda.copy(alpha = 0.10f),
                                border = BorderStroke(1.dp, verdeEsmeralda.copy(alpha = 0.25f)),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.VerifiedUser,
                                        contentDescription = null,
                                        tint = verdeEsmeralda,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = "CORREO ELECTRÓNICO",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FuenteMono,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsSecundario,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = {
                                Text(
                                    "ejemplo@empresa.com.sv",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TsNeutro.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = TsPrimario.copy(alpha = 0.10f),
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Email,
                                            contentDescription = null,
                                            tint = TsPrimario,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = TsSuperficieSuave,
                                focusedBorderColor = TsPrimario,
                                unfocusedBorderColor = TsBorde
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(14.dp))

                        // Etiqueta Contraseña
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONTRASEÑA",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FuenteMono,
                                fontWeight = FontWeight.ExtraBold,
                                color = TsSecundario
                            )
                            Surface(
                                onClick = onRecuperarPasswordClick,
                                shape = RoundedCornerShape(50),
                                color = TsPrimario.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "¿Olvidaste tu contraseña?",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TsPrimario,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = {
                                Text(
                                    "••••••••",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TsNeutro.copy(alpha = 0.6f)
                                )
                            },
                            leadingIcon = {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = TsPrimario.copy(alpha = 0.10f),
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = null,
                                            tint = TsPrimario,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            visualTransformation = if (showPassword) VisualTransformation.None
                            else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Mostrar u ocultar contraseña",
                                        tint = TsNeutro
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = TsSuperficieSuave,
                                focusedBorderColor = TsPrimario,
                                unfocusedBorderColor = TsBorde
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Alertas de Error o Información
                        state.error?.let {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = TsErrorFondo,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, TsErrorTexto.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = it,
                                    color = TsErrorTexto,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        state.info?.let {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                color = TsExitoFondo,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, TsExitoTexto.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = it,
                                    color = TsExitoTexto,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Botón Principal de Acceso
                        Button(
                            onClick = { vm.login(email, password) },
                            enabled = !state.loading && formListo,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = verdeEsmeralda,
                                contentColor = Color.White,
                                disabledContainerColor = azulNoche.copy(alpha = 0.12f),
                                disabledContentColor = TsSecundario.copy(alpha = 0.55f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                disabledElevation = 0.dp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (state.loading) {
                                CircularProgressIndicator(
                                    Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Iniciar Sesión Segura",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Surface(
                onClick = onGoToRegister,
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.22f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TsPrimario.copy(alpha = 0.10f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = null,
                                tint = TsPrimario,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¿Nuevo en Techsmart?",
                            style = MaterialTheme.typography.labelSmall,
                            color = TsNeutro
                        )
                        Text(
                            text = "Crear cuenta corporativa o personal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsPrimario
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = TsSuperficieSuave,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = TsPrimario,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelloHero(texto: String, colorPunto: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(colorPunto)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.90f)
        )
    }
}