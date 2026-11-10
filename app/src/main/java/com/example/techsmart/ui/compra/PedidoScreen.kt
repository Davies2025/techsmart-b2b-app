package com.example.techsmart.ui.compra

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.ui.cart.CartViewModel
import com.example.techsmart.ui.components.*
import com.example.techsmart.ui.theme.*

/** Pasos del rastreo de una compra, en orden. */
private data class Paso(val titulo: String, val icono: ImageVector)

private val PASOS = listOf(
    Paso("Pagado", Icons.Outlined.Payments),
    Paso("En Bodega", Icons.Outlined.Inventory2),
    Paso("En Ruta", Icons.Outlined.LocalShipping),
    Paso("Entregado", Icons.Outlined.TaskAlt)
)

/** Índice del paso actual según el estado (-1 = falta el pago). */
private fun pasoActual(estado: String): Int = when (estado) {
    Cotizacion.ESTADO_APROBADA -> 0
    Cotizacion.ESTADO_EN_BODEGA -> 1
    Cotizacion.ESTADO_DESPACHADA -> 2
    Cotizacion.ESTADO_ENTREGADA -> 3
    else -> -1
}

/**
- Confirmación de la compra y seguimiento del pedido.
- Se actualiza en tiempo real cuando un administrador cambia el estado desde la web.
 */
@Composable
fun PedidoScreen(
    folio: String,
    viewModel: CartViewModel,
    onVolverInicio: () -> Unit,
    onVerCotizaciones: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.observarMisCotizaciones()
        viewModel.observarMisPagos()
    }

    val pedido = state.misCotizaciones.find { it.folio == folio }
    val pago = state.misPagos.find { it.pedidoId == pedido?.id || it.folioPedido == folio }

    val verdeEsmeralda = Color(0xFF0A876C)
    val azulReal = Color(0xFF155EEF)

    if (pedido == null) {
        Box(Modifier.fillMaxSize().background(TsFondo), contentAlignment = Alignment.Center) {
            if (state.cargandoCotizaciones || state.misCotizaciones.isEmpty()) CircularProgressIndicator(color = TsPrimario)
            else MensajeVacio("No se encontró el pedido $folio.")
        }
        return
    }

    val pendientePago = pedido.estado == Cotizacion.ESTADO_PENDIENTE_PAGO
    val cancelado = pedido.estado == Cotizacion.ESTADO_CANCELADA

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
                            Color(0xFFEAF1FF),
                            TsFondo,
                            Color(0xFFE8F5F1)
                        )
                    )
                )
                .padding(padding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 28.dp.toPx()
                val dotRadius = 1.2.dp.toPx()
                val dotColor = TsPrimario.copy(alpha = 0.06f)

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
                        colors = listOf(azulReal.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.10f),
                        radius = size.width * 0.55f
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.10f),
                    radius = size.width * 0.55f
                )
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderPedidoVip(pedido = pedido, cancelado = cancelado, pendientePago = pendientePago)

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.20f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("MONTO TOTAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = TsNeutro)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(dinero(pedido.total), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = TsSecundario)
                                    Text(" USD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TsPrimario, modifier = Modifier.padding(bottom = 4.dp))
                                }
                            }
                            EstadoChip(pedido.estado)
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(
                            "IVA (13%) incluido: ${dinero(pedido.iva)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TsNeutro
                        )

                        Spacer(Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DatoCaja(Icons.Outlined.CreditCard, "Método", pedido.pagoReferencia, Modifier.weight(1f))
                            DatoCaja(Icons.Outlined.CalendarToday, "Fecha", pedido.fecha, Modifier.weight(1f))
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(TsAzulSuave)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconoEnCaja(Icons.AutoMirrored.Outlined.ReceiptLong, tamano = 40.dp, fondo = TsPrimario, tinte = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    pedido.documentoFiscal.ifBlank { "Documento fiscal" },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TsSecundario
                                )
                                Text(
                                    "${pedido.razonSocial} · ${pedido.nit}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TsNeutro,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        if (pedido.vencimiento.isNotBlank() && pendientePago) {
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TsAvisoFondo,
                                border = BorderStroke(1.dp, TsAvisoTexto.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Fecha límite de pago: ${pedido.vencimiento.substringBefore(" (")}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TsAvisoTexto,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // ---------- Comprobante del pago
                if (pago != null) ComprobantePago(pago)

                // ---------- Rastreo ----------
                if (!cancelado) Rastreo(pasoActual(pedido.estado))

                // ---------- Acciones ----------
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onVolverInicio,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = verdeEsmeralda, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Outlined.Home, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Volver al Inicio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onVerCotizaciones,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color.White, contentColor = TsSecundario),
                    border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Outlined.RequestQuote, null, Modifier.size(18.dp), tint = TsPrimario)
                    Spacer(Modifier.width(10.dp))
                    Text("Ver en Mis Cotizaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun HeaderPedidoVip(pedido: Cotizacion, cancelado: Boolean, pendientePago: Boolean) {
    val azulNoche = Color(0xFF0A1931)
    val azulReal = Color(0xFF155EEF)
    val cianPulso = Color(0xFF00D2FF)
    val verdeMenta = Color(0xFF34D399)

    val icono = when {
        cancelado -> Icons.Outlined.Cancel
        pendientePago -> Icons.Outlined.HourglassTop
        else -> Icons.Filled.Check
    }
    val colorIcono = when {
        cancelado -> Color(0xFFEF4444)
        pendientePago -> Color(0xFFF59E0B)
        else -> verdeMenta
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 8.dp,
        border = BorderStroke(1.2.dp, cianPulso.copy(alpha = 0.40f)),
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
                val cy = size.height * 0.30f
                drawCircle(
                    color = cianPulso.copy(alpha = 0.15f),
                    radius = size.width * 0.32f,
                    center = Offset(cx, cy)
                )
            }

            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = colorIcono.copy(alpha = 0.20f),
                    border = BorderStroke(1.5.dp, colorIcono.copy(alpha = 0.5f)),
                    modifier = Modifier.size(72.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icono, null, tint = colorIcono, modifier = Modifier.size(36.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "ORDEN #${pedido.folio}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FuenteMono,
                        fontWeight = FontWeight.ExtraBold,
                        color = cianPulso,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = when {
                        cancelado -> "Pedido cancelado"
                        pendientePago -> "¡Pedido registrado con éxito!"
                        else -> "¡Pago completado y confirmado!"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = when {
                        cancelado -> "La orden #${pedido.folio} fue cancelada por el sistema o administración."
                        pendientePago -> "Tu orden #${pedido.folio} está en espera de la confirmación de transferencia (3 días hábiles)."
                        else -> "Tu orden #${pedido.folio} se encuentra procesándose de forma segura."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DatoCaja(icono: ImageVector, titulo: String, valor: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = TsSuperficieSuave,
        border = BorderStroke(1.dp, TsBorde)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, null, tint = TsPrimario, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(titulo, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TsNeutro)
                Text(valor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TsSecundario, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun Rastreo(actual: Int) {
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.20f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.TrackChanges, null, tint = TsPrimario, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Rastreo del Pedido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TsSecundario,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaMono(
                    if (actual < 0) "Esperando pago" else "Paso ${actual + 1} de ${PASOS.size}",
                    fondo = if (actual >= 0) verdeEsmeralda.copy(alpha = 0.12f) else TsSuperficieSuave,
                    color = if (actual >= 0) verdeEsmeralda else TsNeutro
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                PASOS.forEachIndexed { i, paso ->
                    val hecho = i < actual
                    val enCurso = i == actual
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(if (i == 0) Color.Transparent else if (i <= actual) verdeEsmeralda else TsBorde)
                            )
                            Box(
                                Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            hecho -> verdeEsmeralda
                                            enCurso -> TsPrimario
                                            else -> TsSuperficieSuave
                                        }
                                    )
                                    .then(if (enCurso) Modifier.border(2.5.dp, TsAzulSuave, CircleShape) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (hecho) Icons.Filled.Check else paso.icono,
                                    null,
                                    tint = if (hecho || enCurso) Color.White else TsNeutro,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(if (i == PASOS.lastIndex) Color.Transparent else if (i < actual) verdeEsmeralda else TsBorde)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            paso.titulo,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (enCurso) FontWeight.ExtraBold else FontWeight.Medium,
                            color = when {
                                enCurso -> TsPrimario
                                hecho -> TsSecundario
                                else -> TsNeutro
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = TsSuperficieSuave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (actual < 0) "El rastreo empieza automáticamente cuando se confirme el pago."
                    else "Actualizado por el equipo de Techsmart.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TsNeutro,
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Comprobante del pago guardado en Firestore ('pagos'), enlazado a este pedido. */
@Composable
private fun ComprobantePago(pago: com.example.techsmart.data.model.Pago) {
    val aprobado = pago.estado == com.example.techsmart.data.model.Pago.ESTADO_APROBADO
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, if (aprobado) verdeEsmeralda.copy(alpha = 0.3f) else TsPrimario.copy(alpha = 0.20f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CreditCard, null, tint = if (aprobado) verdeEsmeralda else TsPrimario, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Comprobante de pago",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TsSecundario,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaMono(
                    pago.estado,
                    fondo = if (aprobado) TsExitoFondo else TsAvisoFondo,
                    color = if (aprobado) TsExitoTexto else TsAvisoTexto
                )
            }

            Spacer(Modifier.height(12.dp))

            CajaSuave {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilaMonto("No. de comprobante", pago.id.take(8).uppercase())
                    FilaMonto("Método", pago.referencia)
                    FilaMonto("Fecha", (pago.fecha + " " + pago.hora).trim())
                    FilaMonto("Monto pagado", dinero(pago.monto), destacado = true)
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                if (aprobado) "Pago registrado exitosamente en tu cuenta."
                else "Pendiente de validación bancaria al recibir tu transferencia.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (aprobado) TsExitoTexto else TsAvisoTexto
            )
        }
    }
}