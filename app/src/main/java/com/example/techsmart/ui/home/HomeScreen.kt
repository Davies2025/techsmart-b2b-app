package com.example.techsmart.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techsmart.R
import com.example.techsmart.ui.components.IconoEnCaja
import com.example.techsmart.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onIrACatalogo: () -> Unit,
    onIrACotizaciones: () -> Unit,
    onIrAReclamos: () -> Unit
) {
    val verdeEsmeralda = Color(0xFF0A876C)
    val verdeClaro = Color(0xFFD5F3EA)
    val azulReal = Color(0xFF155EEF)
    val azulClaro = Color(0xFFDCE8FF)
    val cianPulso = Color(0xFF00D2FF)
    val azulNoche = Color(0xFF0A1931)

    // Se obtiene el primer nombre

    val primerNombre = remember {
        FirebaseAuth.getInstance().currentUser?.displayName
            ?.trim()
            ?.split(" ")
            ?.firstOrNull()
            ?.replaceFirstChar { it.uppercase() }
            ?.takeIf { it.isNotBlank() }
    }

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

        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 26.dp.toPx()
            val dotRadius = 1.3.dp.toPx()
            val dotColor = TsPrimario.copy(alpha = 0.08f)

            var x = step / 2f
            while (x < size.width) {
                var y = step / 2f
                while (y < size.height) {
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
                    colors = listOf(azulReal.copy(alpha = 0.10f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.12f),
                    radius = size.width * 0.6f
                ),
                center = Offset(size.width * 0.85f, size.height * 0.12f),
                radius = size.width * 0.6f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(verdeEsmeralda.copy(alpha = 0.09f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.82f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.15f, size.height * 0.82f),
                radius = size.width * 0.55f
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 5.dp,
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                azulReal.copy(alpha = 0.28f),
                                cianPulso.copy(alpha = 0.45f)
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color(0xFFF2F7FF),
                                        Color(0xFFE6F4FF)
                                    )
                                )
                            )
                    ) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val w = size.width
                            val h = size.height

                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(cianPulso.copy(alpha = 0.18f), Color.Transparent),
                                    center = Offset(w * 0.92f, h * 0.5f),
                                    radius = h * 0.9f
                                ),
                                center = Offset(w * 0.92f, h * 0.5f),
                                radius = h * 0.9f
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(azulReal, cianPulso)
                                            )
                                        )
                                )

                                Spacer(Modifier.width(12.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(verdeEsmeralda)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "APLICACIÓN B2B & EXPRESS",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            letterSpacing = 0.9.sp,
                                            color = azulReal
                                        )
                                    }

                                    Spacer(Modifier.height(2.dp))

                                    Text(
                                        text = if (primerNombre != null) "¡Bienvenido, $primerNombre!"
                                        else "¡Bienvenido a Techsmart!",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TsSecundario,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "Descubre las mejores opciones en tecnología.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TsNeutro,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = azulNoche,
                                shadowElevation = 3.dp
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.VerifiedUser,
                                        contentDescription = null,
                                        tint = cianPulso,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "ACTIVO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp,
                                        letterSpacing = 0.8.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(28.dp),
                shadowElevation = 8.dp,
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(248.dp)
                    .clickable(onClick = onIrACotizaciones)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.cotizacion),
                        contentDescription = "Fondo Cotización",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        azulNoche.copy(alpha = 0.20f),
                                        azulNoche.copy(alpha = 0.60f),
                                        azulNoche.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = azulNoche.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.55f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Box(
                                        Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(cianPulso)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Cotiza Fácil",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "IVA 13%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Generar Cotización",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Precios escalonados y lotes a la medida.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                            Spacer(Modifier.height(14.dp))

                            Button(
                                onClick = onIrACotizaciones,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TsPrimario,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.RequestQuote,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Crear Solicitud",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                BentoCardViva(
                    modifier = Modifier.weight(1f),
                    icono = Icons.Outlined.Inventory2,
                    titulo = "Catálogo",
                    subtitulo = "Stock en bodega",
                    etiqueta = "En línea",
                    degradadoFondo = listOf(verdeClaro, Color(0xFFF2FBF8)),
                    colorAcento = verdeEsmeralda,
                    onClick = onIrACatalogo
                )

                BentoCardViva(
                    modifier = Modifier.weight(1f),
                    icono = Icons.Outlined.VerifiedUser,
                    titulo = "Garantías",
                    subtitulo = "Casos RMA",
                    etiqueta = "Digital",
                    degradadoFondo = listOf(azulClaro, Color(0xFFF3F7FF)),
                    colorAcento = azulReal,
                    onClick = onIrAReclamos
                )
            }

            Surface(
                onClick = onIrACatalogo,
                shape = RoundedCornerShape(20.dp),
                color = azulNoche,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cianPulso.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(cianPulso)
                            )
                        }
                        Column {
                            Text(
                                text = "Stock Certificado OEM",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Series validadas • Despacho inmediato",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.14f),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipCategoria(
    texto: String,
    colorPunto: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, colorPunto.copy(alpha = 0.22f)),
        shadowElevation = 1.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(colorPunto)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TsSecundario,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BentoCardViva(
    modifier: Modifier = Modifier,
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    etiqueta: String,
    degradadoFondo: List<Color>,
    colorAcento: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, colorAcento.copy(alpha = 0.28f))
    ) {
        Box(
            modifier = Modifier
                .background(Brush.verticalGradient(degradadoFondo))
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = colorAcento.copy(alpha = 0.10f),
                    radius = size.width * 0.42f,
                    center = Offset(size.width * 0.90f, size.height * 0.15f)
                )
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconoEnCaja(
                        icono,
                        tamano = 42.dp,
                        fondo = Color.White,
                        tinte = colorAcento
                    )

                    Surface(
                        color = Color.White.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.20f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(colorAcento)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = etiqueta,
                                style = MaterialTheme.typography.labelSmall,
                                color = colorAcento,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TsSecundario
                )
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TsNeutro,
                    maxLines = 1
                )

                Spacer(Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorAcento,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Abrir módulo",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}