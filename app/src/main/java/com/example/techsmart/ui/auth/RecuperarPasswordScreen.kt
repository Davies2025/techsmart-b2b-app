package com.example.techsmart.ui.auth

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.MarkEmailRead
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.ui.theme.*

@Composable
fun RecuperarPasswordScreen(
    viewModel: AuthViewModel,
    onVolver: () -> Unit
) {
    var correo by remember { mutableStateOf("") }
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    val azulAbismo = Color(0xFF061126)
    val azulNoche = Color(0xFF0A1931)
    val azulReal = Color(0xFF155EEF)
    val cianPulso = Color(0xFF00D2FF)
    val verdeEsmeralda = Color(0xFF0A876C)
    val verdeMenta = Color(0xFF34D399)

    val correoValido = correo.trim().contains("@") && correo.trim().contains(".")
    val enlaceEnviado = uiState.info != null

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE4EEFF),
                            TsFondo,
                            Color(0xFFDFF3EC)
                        )
                    )
                )
                .padding(padding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val domeHeight = h * 0.45f

                val step = 26.dp.toPx()
                val dotRadius = 1.3.dp.toPx()
                val dotColor = azulReal.copy(alpha = 0.08f)
                var x = step / 2f
                while (x < w) {
                    var y = domeHeight * 0.85f
                    while (y < h) {
                        drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                        y += step
                    }
                    x += step
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(verdeEsmeralda.copy(alpha = 0.14f), Color.Transparent),
                        center = Offset(w * 0.85f, h * 0.88f),
                        radius = w * 0.65f
                    ),
                    center = Offset(w * 0.85f, h * 0.88f),
                    radius = w * 0.65f
                )

                val domePath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w, 0f)
                    lineTo(w, domeHeight * 0.82f)
                    quadraticTo(
                        w * 0.5f,
                        domeHeight * 1.12f,
                        0f,
                        domeHeight * 0.82f
                    )
                    close()
                }

                drawPath(
                    path = domePath,
                    brush = Brush.linearGradient(
                        colors = listOf(azulAbismo, azulNoche, Color(0xFF0F2C67), azulReal),
                        start = Offset(0f, 0f),
                        end = Offset(w, domeHeight)
                    )
                )

                val borderPath = Path().apply {
                    moveTo(0f, domeHeight * 0.82f)
                    quadraticTo(
                        w * 0.5f,
                        domeHeight * 1.12f,
                        w,
                        domeHeight * 0.82f
                    )
                }
                drawPath(
                    path = borderPath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            cianPulso.copy(alpha = 0.2f),
                            cianPulso.copy(alpha = 0.75f),
                            verdeMenta.copy(alpha = 0.65f),
                            cianPulso.copy(alpha = 0.2f)
                        )
                    ),
                    style = Stroke(width = 2.5.dp.toPx())
                )

                val centerTop = Offset(w * 0.5f, domeHeight * 0.46f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(cianPulso.copy(alpha = 0.20f), Color.Transparent),
                        center = centerTop,
                        radius = w * 0.48f
                    ),
                    center = centerTop,
                    radius = w * 0.48f
                )
                drawCircle(
                    color = cianPulso.copy(alpha = 0.12f),
                    radius = w * 0.28f,
                    center = centerTop,
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCircle(
                    color = verdeMenta.copy(alpha = 0.10f),
                    radius = w * 0.38f,
                    center = centerTop,
                    style = Stroke(width = 1.dp.toPx())
                )

                val stroke = 1.8.dp.toPx()
                drawLine(
                    color = cianPulso.copy(alpha = 0.28f),
                    start = Offset(0f, domeHeight * 0.28f),
                    end = Offset(w * 0.16f, domeHeight * 0.28f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = cianPulso.copy(alpha = 0.28f),
                    start = Offset(w * 0.16f, domeHeight * 0.28f),
                    end = Offset(w * 0.22f, domeHeight * 0.36f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = verdeMenta,
                    radius = 3.5.dp.toPx(),
                    center = Offset(w * 0.22f, domeHeight * 0.36f)
                )

                drawLine(
                    color = cianPulso.copy(alpha = 0.28f),
                    start = Offset(w, domeHeight * 0.24f),
                    end = Offset(w * 0.82f, domeHeight * 0.24f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = cianPulso.copy(alpha = 0.28f),
                    start = Offset(w * 0.82f, domeHeight * 0.24f),
                    end = Offset(w * 0.76f, domeHeight * 0.32f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = cianPulso,
                    radius = 3.5.dp.toPx(),
                    center = Offset(w * 0.76f, domeHeight * 0.32f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onVolver,
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Login",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = verdeMenta.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, verdeMenta.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(verdeMenta)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Contraseña",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FuenteMono,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(92.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = cianPulso.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxSize()
                    ) {}
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF0E295B),
                        shadowElevation = 10.dp,
                        border = BorderStroke(
                            1.5.dp,
                            Brush.linearGradient(listOf(cianPulso, verdeMenta))
                        ),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (enlaceEnviado) Icons.Outlined.MarkEmailRead else Icons.Outlined.LockReset,
                                contentDescription = null,
                                tint = if (enlaceEnviado) verdeMenta else cianPulso,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "ACCESO TECHSMART",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FuenteMono,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = cianPulso
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Recuperar Contraseña",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Restaura las credenciales de tu cuenta corporativa o personal en segundos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(20.dp))

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 14.dp,
                    border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.22f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(azulReal, cianPulso, verdeEsmeralda)
                                    )
                                )
                        )

                        Column(modifier = Modifier.padding(22.dp)) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFFF3F7FF),
                                border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.14f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    PasoRecuperacion(
                                        numero = "1",
                                        titulo = "Correo",
                                        completado = correoValido || enlaceEnviado,
                                        activo = !enlaceEnviado
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 6.dp)
                                            .height(2.dp)
                                            .background(if (correoValido || enlaceEnviado) verdeEsmeralda else TsBorde)
                                    )
                                    PasoRecuperacion(
                                        numero = "2",
                                        titulo = "Enlace",
                                        completado = enlaceEnviado,
                                        activo = correoValido && !enlaceEnviado
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 6.dp)
                                            .height(2.dp)
                                            .background(if (enlaceEnviado) verdeEsmeralda else TsBorde)
                                    )
                                    PasoRecuperacion(
                                        numero = "3",
                                        titulo = "Nueva Clave",
                                        completado = false,
                                        activo = enlaceEnviado
                                    )
                                }
                            }

                            Spacer(Modifier.height(20.dp))

                            // Encabezado del campo con detector en vivo
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CORREO REGISTRADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FuenteMono,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TsSecundario
                                )

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (correoValido) verdeEsmeralda.copy(alpha = 0.12f)
                                    else TsPrimario.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = if (correoValido) "✓ FORMATO VÁLIDO" else "REQUERIDO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FuenteMono,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (correoValido) verdeEsmeralda else TsPrimario,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = correo,
                                onValueChange = {
                                    correo = it
                                    if (uiState.error != null || uiState.info != null) {
                                        viewModel.consumeSuccess()
                                    }
                                },
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
                                        color = if (correoValido) verdeEsmeralda.copy(alpha = 0.12f)
                                        else TsPrimario.copy(alpha = 0.10f),
                                        modifier = Modifier
                                            .padding(start = 6.dp)
                                            .size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Filled.Email,
                                                contentDescription = null,
                                                tint = if (correoValido) verdeEsmeralda else TsPrimario,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                trailingIcon = {
                                    if (correoValido) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = verdeEsmeralda,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = TsSuperficieSuave,
                                    focusedBorderColor = if (correoValido) verdeEsmeralda else TsPrimario,
                                    unfocusedBorderColor = if (correoValido) verdeEsmeralda.copy(alpha = 0.5f) else TsBorde
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (uiState.error != null) {
                                Spacer(Modifier.height(14.dp))
                                Surface(
                                    color = TsErrorFondo,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, TsErrorTexto.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Info,
                                            contentDescription = null,
                                            tint = TsErrorTexto,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = uiState.error!!,
                                            color = TsErrorTexto,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else if (uiState.info != null) {
                                Spacer(Modifier.height(14.dp))
                                Surface(
                                    color = TsExitoFondo,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, TsExitoTexto.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(14.dp)
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = TsExitoTexto.copy(alpha = 0.15f),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Outlined.MarkEmailRead,
                                                    contentDescription = null,
                                                    tint = TsExitoTexto,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "¡Enlace enviado con éxito!",
                                                color = TsExitoTexto,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = uiState.info!!,
                                                color = TsExitoTexto.copy(alpha = 0.9f),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(22.dp))

                            Button(
                                onClick = { viewModel.resetPassword(correo) },
                                enabled = !uiState.loading,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (correoValido) verdeEsmeralda else azulReal,
                                    contentColor = Color.White
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                            ) {
                                if (uiState.loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = if (enlaceEnviado) "Reenviar Enlace de Acceso" else "Enviar Enlace de Recuperación",
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

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = azulNoche,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = cianPulso.copy(alpha = 0.16f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.VerifiedUser,
                                        contentDescription = null,
                                        tint = cianPulso,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Verificación de Bandeja de Entrada",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Si no visualizas el mensaje en 1 minuto, revisa la carpeta de Spam o No deseados.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.78f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Surface(
                    onClick = onVolver,
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.20f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 13.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = TsPrimario,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "¿Recordaste tu clave? ",
                            style = MaterialTheme.typography.bodySmall,
                            color = TsNeutro
                        )
                        Text(
                            text = "Volver a Iniciar Sesión",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsPrimario
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoRecuperacion(
    numero: String,
    titulo: String,
    completado: Boolean,
    activo: Boolean
) {
    val verdeEsmeralda = Color(0xFF0A876C)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when {
                        completado -> verdeEsmeralda
                        activo -> TsPrimario
                        else -> TsBorde
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (completado) "✓" else numero,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FuenteMono,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (completado || activo) Color.White else TsNeutro
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = if (completado || activo) FontWeight.ExtraBold else FontWeight.Medium,
            color = when {
                completado -> verdeEsmeralda
                activo -> TsSecundario
                else -> TsNeutro
            }
        )
    }
}