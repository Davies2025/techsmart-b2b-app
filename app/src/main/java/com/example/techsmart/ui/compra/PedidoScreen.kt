package com.example.techsmart.ui.compra

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
 * Confirmación de la compra y seguimiento del pedido.
 * Se actualiza en tiempo real cuando un administrador cambia el estado desde la web.
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

    if (pedido == null) {
        Box(Modifier.fillMaxSize().background(TsFondo), contentAlignment = Alignment.Center) {
            if (state.cargandoCotizaciones || state.misCotizaciones.isEmpty()) CircularProgressIndicator()
            else MensajeVacio("No se encontró el pedido $folio.")
        }
        return
    }

    val pendientePago = pedido.estado == Cotizacion.ESTADO_PENDIENTE_PAGO
    val cancelado = pedido.estado == Cotizacion.ESTADO_CANCELADA

    Column(
        Modifier
            .fillMaxSize()
            .background(TsFondo)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ---------- Encabezado ----------
        Spacer(Modifier.height(8.dp))
        IconoResultado(
            when {
                cancelado -> Icons.Outlined.Cancel
                pendientePago -> Icons.Outlined.HourglassTop
                else -> Icons.Filled.Check
            },
            color = when {
                cancelado -> TsErrorTexto
                pendientePago -> TsAvisoTexto
                else -> TsPrimario
            }
        )
        EstadoChip(pedido.estado)
        Text(
            when {
                cancelado -> "Pedido cancelado"
                pendientePago -> "¡Pedido registrado!"
                else -> "¡Pago realizado con éxito!"
            },
            style = MaterialTheme.typography.headlineMedium,
            color = TsSecundario,
            textAlign = TextAlign.Center
        )
        Text(
            when {
                cancelado -> "La orden #${pedido.folio} fue cancelada."
                pendientePago -> "Tu orden #${pedido.folio} queda pendiente hasta que confirmemos tu transferencia (3 días)."
                else -> "Tu orden #${pedido.folio} fue pagada y está en proceso."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = TsNeutro,
            textAlign = TextAlign.Center
        )

        // ---------- Resumen del pago ----------
        TarjetaTs(Modifier.fillMaxWidth()) {
            Text("MONTO TOTAL", style = MaterialTheme.typography.labelMedium, color = TsNeutro)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(dinero(pedido.total), style = MaterialTheme.typography.headlineMedium, color = TsSecundario)
                Text(" USD", style = MaterialTheme.typography.labelMedium, color = TsNeutro, modifier = Modifier.padding(bottom = 4.dp))
            }
            Text(
                "IVA (13%) incluido: ${dinero(pedido.iva)}",
                style = MaterialTheme.typography.bodySmall,
                color = TsNeutro
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DatoCaja(Icons.Outlined.CreditCard, "Método", pedido.pagoReferencia, Modifier.weight(1f))
                DatoCaja(Icons.Outlined.CalendarToday, "Fecha", pedido.fecha, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TsAzulSuave)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconoEnCaja(Icons.AutoMirrored.Outlined.ReceiptLong, tamano = 40.dp, fondo = TsPrimario, tinte = Color.White)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        pedido.documentoFiscal.ifBlank { "Documento fiscal" },
                        style = MaterialTheme.typography.titleSmall,
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
                Spacer(Modifier.height(8.dp))
                Text(
                    "Fecha límite de pago: ${pedido.vencimiento.substringBefore(" (")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TsAvisoTexto,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ---------- Comprobante del pago (colección 'pagos') ----------
        if (pago != null) ComprobantePago(pago)

        // ---------- Rastreo ----------
        if (!cancelado) Rastreo(pasoActual(pedido.estado))

        // ---------- Acciones ----------
        Button(
            onClick = onVolverInicio,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Outlined.Home, null, Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Volver al Inicio", style = MaterialTheme.typography.titleSmall)
        }
        FilledTonalButton(
            onClick = onVerCotizaciones,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = TsAzulSuave, contentColor = TsSecundario),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(Icons.Outlined.RequestQuote, null, Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Ver en Mis Cotizaciones", style = MaterialTheme.typography.titleSmall)
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun IconoResultado(icono: ImageVector, color: Color) {
    Box(
        Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(108.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.8f)))),
            contentAlignment = Alignment.Center
        ) {
            Icon(icono, null, tint = Color.White, modifier = Modifier.size(52.dp))
        }
    }
}

@Composable
private fun DatoCaja(icono: ImageVector, titulo: String, valor: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TsSuperficieSuave)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, null, tint = TsPrimario, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TsSecundario)
            Text(valor, style = MaterialTheme.typography.labelSmall, color = TsNeutro, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun Rastreo(actual: Int) {
    TarjetaTs(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.TrackChanges, null, tint = TsPrimario)
            Spacer(Modifier.width(8.dp))
            Text(
                "Rastreo del Pedido",
                style = MaterialTheme.typography.titleMedium,
                color = TsSecundario,
                modifier = Modifier.weight(1f)
            )
            EtiquetaMono(if (actual < 0) "Esperando pago" else "Paso ${actual + 1} de ${PASOS.size}")
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            PASOS.forEachIndexed { i, paso ->
                val hecho = i < actual
                val enCurso = i == actual
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        // Línea a la izquierda (no en el primer paso)
                        Box(
                            Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (i == 0) Color.Transparent else if (i <= actual) TsPrimario else TsBorde)
                        )
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        hecho -> TsSecundario
                                        enCurso -> TsPrimario
                                        else -> TsSuperficieSuave
                                    }
                                )
                                .then(if (enCurso) Modifier.border(3.dp, TsAzulSuave, CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (hecho) Icons.Filled.Check else paso.icono,
                                null,
                                tint = if (hecho || enCurso) Color.White else TsNeutro,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(if (i == PASOS.lastIndex) Color.Transparent else if (i < actual) TsPrimario else TsBorde)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        paso.titulo,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (enCurso) FontWeight.Bold else FontWeight.Medium,
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
        Text(
            if (actual < 0) "El rastreo empieza cuando se confirme el pago."
            else "El estado lo actualiza el equipo de Techsmart; esta pantalla se actualiza sola.",
            style = MaterialTheme.typography.bodySmall,
            color = TsNeutro
        )
    }
}

/** Comprobante del pago guardado en Firestore ('pagos'), enlazado a este pedido. */
@Composable
private fun ComprobantePago(pago: com.example.techsmart.data.model.Pago) {
    val aprobado = pago.estado == com.example.techsmart.data.model.Pago.ESTADO_APROBADO
    TarjetaTs(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CreditCard, null, tint = TsPrimario)
            Spacer(Modifier.width(8.dp))
            Text(
                "Comprobante de pago",
                style = MaterialTheme.typography.titleMedium,
                color = TsSecundario,
                modifier = Modifier.weight(1f)
            )
            EtiquetaMono(
                pago.estado,
                fondo = if (aprobado) TsExitoFondo else TsAvisoFondo,
                color = if (aprobado) TsExitoTexto else TsAvisoTexto
            )
        }
        Spacer(Modifier.height(10.dp))
        CajaSuave {
            FilaMonto("No. de comprobante", pago.id.take(8).uppercase())
            FilaMonto("Método", pago.referencia)
            FilaMonto("Fecha", (pago.fecha + " " + pago.hora).trim())
            FilaMonto("Monto pagado", dinero(pago.monto), destacado = true)
        }
        Text(
            if (aprobado) "Pago registrado en tu cuenta."
            else "Se confirmará al recibir tu transferencia.",
            style = MaterialTheme.typography.bodySmall,
            color = if (aprobado) TsExitoTexto else TsAvisoTexto,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
