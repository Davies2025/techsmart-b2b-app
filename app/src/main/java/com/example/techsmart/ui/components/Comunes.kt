package com.example.techsmart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.techsmart.ui.theme.*
import java.util.Locale

/** "$1,234.50" (el dólar es la moneda de El Salvador). */
fun dinero(valor: Double): String = String.format(Locale.US, "$%,.2f", valor)

// ---------------------------------------------------------------------------
// Marca
// ---------------------------------------------------------------------------

/** Logo de Techsmart: cuadro azul con una "T" y un punto cian. */
@Composable
fun LogoTechsmart(tamano: Dp = 40.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(tamano)
            .clip(RoundedCornerShape(tamano * 0.28f))
            .background(TsPrimario),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "T",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = (tamano.value * 0.55f).sp
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = tamano * 0.2f, bottom = tamano * 0.22f)
                .size(tamano * 0.16f)
                .clip(CircleShape)
                .background(TsTerciario)
        )
    }
}

/**
 * Barra superior común: logo, "Techsmart", subtítulo de la sección y botón de cuenta.
 * El menú de cuenta muestra el correo de la sesión y la opción de cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperiorTechsmart(
    subtitulo: String,
    correoSesion: String?,
    onCerrarSesion: () -> Unit
) {
    var menuAbierto by remember { mutableStateOf(false) }

    Surface(color = TsSuperficie, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoTechsmart(36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "Techsmart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TsSecundario
                )
                Text(
                    subtitulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TsNeutro
                )
            }
            Box {
                FilledIconButton(
                    onClick = { menuAbierto = true },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = TsPrimario)
                ) {
                    Icon(Icons.Outlined.Person, contentDescription = "Mi cuenta", tint = Color.White)
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    if (!correoSesion.isNullOrBlank()) {
                        Text(
                            "Sesión iniciada como",
                            style = MaterialTheme.typography.labelSmall,
                            color = TsNeutro,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        Text(
                            correoSesion,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    }
                    DropdownMenuItem(
                        text = { Text("Cerrar sesión", color = TsErrorTexto) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = TsErrorTexto)
                        },
                        onClick = { menuAbierto = false; onCerrarSesion() }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Contenedores
// ---------------------------------------------------------------------------

/** Tarjeta blanca con borde suave, la base de casi todos los bloques del diseño. */
@Composable
fun TarjetaTs(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(20.dp)
    val borde = BorderStroke(1.dp, TsBorde)
    val colores = CardDefaults.cardColors(containerColor = TsSuperficie)
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = forma, border = borde, colors = colores) {
            Column(Modifier.padding(16.dp), content = contenido)
        }
    } else {
        Card(modifier = modifier, shape = forma, border = borde, colors = colores) {
            Column(Modifier.padding(16.dp), content = contenido)
        }
    }
}

/** Caja gris clara dentro de una tarjeta (resúmenes, descripciones). */
@Composable
fun CajaSuave(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TsSuperficieSuave)
            .padding(12.dp),
        content = contenido
    )
}

/** Ícono dentro de un cuadro azul claro (encabezados de tarjetas). */
@Composable
fun IconoEnCaja(icono: ImageVector, tamano: Dp = 44.dp, fondo: Color = TsAzulSuave, tinte: Color = TsPrimario) {
    Box(
        Modifier
            .size(tamano)
            .clip(RoundedCornerShape(12.dp))
            .background(fondo),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = tinte, modifier = Modifier.size(tamano * 0.5f))
    }
}

/**
 * Banner azul claro de la cabecera de cada sección
 * (p. ej. "Cotizador B2B & Express", "Garantía Techsmart Oficial").
 */
@Composable
fun BannerSeccion(icono: ImageVector, titulo: String, descripcion: String, etiqueta: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(TsAzulSuave)
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

/** Encabezado de lista: ícono + título + contador ("3 EN CURSO"). */
@Composable
fun EncabezadoSeccion(icono: ImageVector, titulo: String, contador: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = TsPrimario, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            titulo,
            style = MaterialTheme.typography.titleLarge,
            color = TsSecundario,
            modifier = Modifier.weight(1f)
        )
        if (contador != null) EtiquetaMono(contador)
    }
}

// ---------------------------------------------------------------------------
// Etiquetas
// ---------------------------------------------------------------------------

/** Etiqueta pequeña en monoespaciada sobre fondo azul claro ("SLA 48H", "3 EN CURSO"). */
@Composable
fun EtiquetaMono(texto: String, fondo: Color = TsInfoFondo, color: Color = TsInfoTexto) {
    Surface(color = fondo, shape = RoundedCornerShape(50)) {
        Text(
            texto.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

/** Colores (fondo, texto) para cada estado de cotización o reclamo. */
fun coloresEstado(estado: String): Pair<Color, Color> = when (estado) {
    "Aprobada", "Despachada", "Entregada", "Cambio Aprobado", "Cerrado" -> TsExitoFondo to TsExitoTexto
    "Cancelada" -> TsErrorFondo to TsErrorTexto
    "Pendiente de Pago", "En Bodega", "Recibido en Bodega", "En Diagnóstico Técnico" ->
        TsAvisoFondo to TsAvisoTexto
    else -> TsInfoFondo to TsInfoTexto // "En Revisión", "Reportado"
}

/** Chip de estado con punto de color ("● En Revisión"). */
@Composable
fun EstadoChip(estado: String, modifier: Modifier = Modifier) {
    val (fondo, texto) = coloresEstado(estado)
    Surface(color = fondo, shape = RoundedCornerShape(50), modifier = modifier) {
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

/** Fila "Etiqueta ........ valor" para desgloses. */
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

/** Texto para listas vacías o estados de carga. */
@Composable
fun MensajeVacio(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = TsNeutro,
        modifier = modifier.padding(24.dp)
    )
}

/** Etiqueta de campo de formulario (texto pequeño en gris sobre el campo). */
@Composable
fun EtiquetaCampo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = TsNeutro,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

/** Campo de texto con el estilo del diseño: relleno gris claro y sin borde. */
@Composable
fun CampoTs(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardOptions: androidx.compose.foundation.text.KeyboardOptions =
        androidx.compose.foundation.text.KeyboardOptions.Default
) {
    TextField(
        value = valor,
        onValueChange = onCambio,
        placeholder = { Text(placeholder, color = TsNeutro.copy(alpha = 0.7f)) },
        leadingIcon = leadingIcon?.let { { Icon(it, null, tint = TsNeutro) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TsSuperficieSuave,
            unfocusedContainerColor = TsSuperficieSuave,
            disabledContainerColor = TsSuperficieSuave,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** Selector de dos o más opciones en una barra (p. ej. Empresa / Particular). */
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
            .clip(RoundedCornerShape(12.dp))
            .background(TsSuperficieSuave)
            .padding(4.dp)
    ) {
        opciones.forEach { opcion ->
            val activa = opcion == seleccion
            Surface(
                onClick = { onSeleccion(opcion) },
                color = if (activa) TsPrimario else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
