package com.example.techsmart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techsmart.R
import com.example.techsmart.ui.theme.*
import java.util.Locale

/** "$1,234.50" (el dólar es la moneda de El Salvador). */
fun dinero(valor: Double): String = String.format(Locale.US, "$%,.2f", valor)

@Composable
fun LogoTechsmart(tamano: Dp = 42.dp, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(tamano * 0.30f),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.18f)),
        modifier = modifier.size(tamano)
    ) {
        Image(
            painter = painterResource(id = R.drawable.techsmart_app_img),
            contentDescription = "Logo Techsmart",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(tamano * 0.30f))
                .scale(1.15f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperiorTechsmart(
    subtitulo: String,
    correoSesion: String? = null,
    onCerrarSesion: (() -> Unit)? = null
) {
    var menuAbierto by remember { mutableStateOf(false) }
    val cianPulso = Color(0xFF00C2FF)
    val verdeEnLinea = Color(0xFF0A876C)

    val inicialUsuario = remember(correoSesion) {
        correoSesion?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    }

    Surface(
        color = TsSuperficie,
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    LogoTechsmart(tamano = 42.dp)

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Techsmart",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = TsSecundario
                            )
                            Spacer(Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(TsPrimario)
                            )
                        }

                        Text(
                            text = subtitulo,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TsPrimario
                        )
                    }
                }

                if (onCerrarSesion != null) {
                    Box {
                        Surface(
                            onClick = { menuAbierto = true },
                            shape = RoundedCornerShape(50),
                            color = TsPrimario.copy(alpha = 0.07f),
                            border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.18f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 5.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Box(modifier = Modifier.size(32.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(TsPrimario, Color(0xFF00227B))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = inicialUsuario,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(verdeEnLinea)
                                            .border(1.5.dp, Color.White, CircleShape)
                                    )
                                }

                                Spacer(Modifier.width(4.dp))

                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Abrir menú de usuario",
                                    tint = TsSecundario,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = menuAbierto,
                            onDismissRequest = { menuAbierto = false },
                            shape = RoundedCornerShape(18.dp),
                            containerColor = TsSuperficie,
                            tonalElevation = 4.dp,
                            shadowElevation = 8.dp,
                            modifier = Modifier.width(250.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(TsPrimario.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = inicialUsuario,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TsPrimario
                                        )
                                    }

                                    Spacer(Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(verdeEnLinea)
                                            )
                                            Spacer(Modifier.width(5.dp))
                                            Text(
                                                text = "Sesión activa",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = verdeEnLinea
                                            )
                                        }
                                        if (!correoSesion.isNullOrBlank()) {
                                            Text(
                                                text = correoSesion,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TsSecundario,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(12.dp))
                                HorizontalDivider(color = TsNeutro.copy(alpha = 0.15f))
                                Spacer(Modifier.height(10.dp))

                                Surface(
                                    onClick = {
                                        menuAbierto = false
                                        onCerrarSesion()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Cerrar sesión",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "Cerrar sesión",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                TsPrimario.copy(alpha = 0.35f),
                                cianPulso.copy(alpha = 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Contenedores
// ---------------------------------------------------------------------------

@Composable
fun TarjetaTs(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(20.dp)
    val borde = BorderStroke(1.dp, TsBorde)
    val colores = CardDefaults.cardColors(containerColor = TsSuperficie)
    val elevacion = CardDefaults.cardElevation(defaultElevation = 2.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = forma,
            border = borde,
            colors = colores,
            elevation = elevacion
        ) {
            Column(Modifier.padding(16.dp), content = contenido)
        }
    } else {
        Card(
            modifier = modifier,
            shape = forma,
            border = borde,
            colors = colores,
            elevation = elevacion
        ) {
            Column(Modifier.padding(16.dp), content = contenido)
        }
    }
}

@Composable
fun CajaSuave(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TsSuperficieSuave)
            .border(1.dp, TsBorde.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        content = contenido
    )
}

@Composable
fun IconoEnCaja(
    icono: ImageVector,
    tamano: Dp = 44.dp,
    fondo: Color = TsAzulSuave,
    tinte: Color = TsPrimario
) {
    Box(
        Modifier
            .size(tamano)
            .clip(RoundedCornerShape(13.dp))
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(tamano * 0.5f))
    }
}

@Composable
fun BannerSeccion(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    etiqueta: String? = null
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.18f)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(TsAzulSuave, Color.White)
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconoEnCaja(icono, fondo = TsPrimario, tinte = Color.White)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TsSecundario,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (etiqueta != null) {
                        Spacer(Modifier.width(8.dp))
                        EtiquetaMono(etiqueta)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TsNeutro)
            }
        }
    }
}

@Composable
fun EncabezadoSeccion(icono: ImageVector, titulo: String, contador: String? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = TsPrimario, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TsSecundario,
            modifier = Modifier.weight(1f)
        )
        if (contador != null) EtiquetaMono(contador)
    }
}

// ---------------------------------------------------------------------------
// Etiquetas
// ---------------------------------------------------------------------------

@Composable
fun EtiquetaMono(texto: String, fondo: Color = TsInfoFondo, color: Color = TsInfoTexto) {
    Surface(color = fondo, shape = RoundedCornerShape(50)) {
        Text(
            texto.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

fun coloresEstado(estado: String): Pair<Color, Color> = when (estado) {
    "Aprobada", "Despachada", "Entregada", "Cambio Aprobado", "Cerrado" -> TsExitoFondo to TsExitoTexto
    "Cancelada" -> TsErrorFondo to TsErrorTexto
    "Pendiente de Pago", "En Bodega", "Recibido en Bodega", "En Diagnóstico Técnico" ->
        TsAvisoFondo to TsAvisoTexto
    else -> TsInfoFondo to TsInfoTexto
}

@Composable
fun EstadoChip(estado: String, modifier: Modifier = Modifier) {
    val (fondo, texto) = coloresEstado(estado)
    Surface(
        color = fondo,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, texto.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(texto))
            Spacer(Modifier.width(6.dp))
            Text(
                estado,
                color = texto,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Varios
// ---------------------------------------------------------------------------
@Composable
fun FilaMonto(
    etiqueta: String,
    valor: String,
    destacado: Boolean = false,
    colorValor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val estilo = if (destacado) MaterialTheme.typography.titleMedium
        else MaterialTheme.typography.bodyMedium
        Text(etiqueta, style = estilo, color = if (destacado) TsSecundario else TsNeutro)
        Text(
            valor,
            style = estilo,
            color = if (colorValor == Color.Unspecified) TsSecundario else colorValor,
            fontWeight = if (destacado) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}

@Composable
fun MensajeVacio(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = TsNeutro,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    )
}

@Composable
fun EtiquetaCampo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
        color = TsSecundario,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun CampoTs(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    TextField(
        value = valor,
        onValueChange = onCambio,
        placeholder = { Text(placeholder, color = TsNeutro.copy(alpha = 0.7f)) },
        leadingIcon = leadingIcon?.let { { Icon(it, null, tint = TsPrimario) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(14.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TsSuperficieSuave,
            unfocusedContainerColor = TsSuperficieSuave,
            disabledContainerColor = TsSuperficieSuave,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, TsBorde, RoundedCornerShape(14.dp))
    )
}

/** Selector de dos o más opciones en una barra */
@Composable
fun SelectorSegmentado(
    opciones: List<String>,
    seleccion: String,
    onSeleccion: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TsSuperficieSuave)
            .border(1.dp, TsBorde, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        opciones.forEach { opcion ->
            val activa = opcion == seleccion
            Surface(
                onClick = { onSeleccion(opcion) },
                color = if (activa) TsPrimario else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                shadowElevation = if (activa) 2.dp else 0.dp,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    opcion,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium,
                    color = if (activa) Color.White else TsNeutro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}