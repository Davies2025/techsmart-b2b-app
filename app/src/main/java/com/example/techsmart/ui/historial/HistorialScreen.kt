package com.example.techsmart.ui.historial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.Pago
import com.example.techsmart.domain.engine.TieredPricingEngine
import com.example.techsmart.ui.cart.CartViewModel
import com.example.techsmart.ui.components.*
import com.example.techsmart.ui.theme.*

private const val TAB_COMPRAS = "Compras"
private const val TAB_COTIZACIONES = "Cotizaciones"
private const val TAB_PAGOS = "Pagos"

/**
 * Historial de la cuenta con sesión iniciada: compras, cotizaciones y pagos.
 * Todo se lee de Firestore filtrado por usuarioCorreo y se actualiza en tiempo real.
 */
@Composable
fun HistorialScreen(
    viewModel: CartViewModel,
    onVerPedido: (folio: String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pestana by rememberSaveable { mutableStateOf(TAB_COMPRAS) }

    LaunchedEffect(Unit) {
        viewModel.observarMisCotizaciones()
        viewModel.observarMisPagos()
    }

    val compras = state.misCotizaciones.filter { it.esCompra }
    val cotizaciones = state.misCotizaciones.filterNot { it.esCompra }
    val pagos = state.misPagos
    val totalPagado = TieredPricingEngine.centavos(
        pagos.filter { it.estado == Pago.ESTADO_APROBADO }.sumOf { TieredPricingEngine.centavos(it.monto) }
    )
    val totalPendiente = TieredPricingEngine.centavos(
        pagos.filter { it.estado == Pago.ESTADO_PENDIENTE }.sumOf { TieredPricingEngine.centavos(it.monto) }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BannerSeccion(
                icono = Icons.Outlined.History,
                titulo = "Mi Historial",
                descripcion = "Tus compras, cotizaciones y pagos registrados en tu cuenta."
            )
        }

        // ---------- Resumen ----------
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Resumen("Total pagado", dinero(totalPagado), Modifier.weight(1.3f), destacado = true)
                Resumen("Compras", "${compras.size}", Modifier.weight(1f))
                Resumen("Cotizaciones", "${cotizaciones.size}", Modifier.weight(1f))
            }
            if (totalPendiente > 0.0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pendiente de confirmar por transferencia: ${dinero(totalPendiente)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TsAvisoTexto
                )
            }
        }

        item {
            SelectorSegmentado(
                opciones = listOf(TAB_COMPRAS, TAB_COTIZACIONES, TAB_PAGOS),
                seleccion = pestana,
                onSeleccion = { pestana = it }
            )
        }

        if (state.cargandoCotizaciones && state.misCotizaciones.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        } else when (pestana) {
            TAB_COMPRAS ->
                if (compras.isEmpty()) item { MensajeVacio("Aún no tienes compras.") }
                else items(compras, key = { "c-" + it.id.ifBlank { it.folio } }) { compra ->
                    TarjetaCompra(compra, pagos.find { it.pedidoId == compra.id || it.folioPedido == compra.folio }) {
                        onVerPedido(compra.folio)
                    }
                }

            TAB_COTIZACIONES ->
                if (cotizaciones.isEmpty()) item { MensajeVacio("Aún no tienes cotizaciones.") }
                else items(cotizaciones, key = { "q-" + it.id.ifBlank { it.folio } }) { TarjetaCotizacion(it) }

            else ->
                if (pagos.isEmpty()) item { MensajeVacio("Aún no tienes pagos registrados.") }
                else items(pagos, key = { "p-" + it.id }) { pago ->
                    TarjetaPago(pago) { onVerPedido(pago.folioPedido) }
                }
        }
    }
}

// ---------------------------------------------------------------------------
// Partes
// ---------------------------------------------------------------------------

@Composable
private fun Resumen(titulo: String, valor: String, modifier: Modifier, destacado: Boolean = false) {
    Surface(
        color = if (destacado) TsPrimario else TsSuperficie,
        shape = RoundedCornerShape(16.dp),
        border = if (destacado) null else androidx.compose.foundation.BorderStroke(1.dp, TsBorde),
        modifier = modifier
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            Text(
                titulo,
                style = MaterialTheme.typography.labelSmall,
                color = if (destacado) Color.White.copy(alpha = 0.85f) else TsNeutro,
                maxLines = 1
            )
            Text(
                valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = if (destacado) Color.White else TsSecundario,
                maxLines = 1
            )
        }
    }
}

private fun resumenItems(c: Cotizacion): String =
    c.items.joinToString(" + ") { "${it.cantidad}x ${it.nombreMostrado}" }

@Composable
private fun TarjetaCompra(compra: Cotizacion, pago: Pago?, onClick: () -> Unit) {
    TarjetaTs(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "#${compra.folio}",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FuenteMono,
                fontWeight = FontWeight.Bold,
                color = TsSecundario,
                modifier = Modifier.weight(1f)
            )
            EstadoChip(compra.estado)
        }
        Text(
            "${compra.fecha} · ${compra.pagoReferencia}",
            style = MaterialTheme.typography.bodySmall,
            color = TsNeutro,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(8.dp))
        CajaSuave {
            Text(
                resumenItems(compra),
                style = MaterialTheme.typography.bodyMedium,
                color = TsSecundario,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            FilaMonto("Total", dinero(compra.total), destacado = true)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                when (pago?.estado) {
                    Pago.ESTADO_APROBADO -> "Pago registrado"
                    Pago.ESTADO_PENDIENTE -> "Pago pendiente de confirmar"
                    else -> "Sin comprobante de pago"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when (pago?.estado) {
                    Pago.ESTADO_APROBADO -> TsExitoTexto
                    Pago.ESTADO_PENDIENTE -> TsAvisoTexto
                    else -> TsNeutro
                },
                modifier = Modifier.weight(1f)
            )
            Text("Seguimiento", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TsPrimario)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TsPrimario)
        }
    }
}

@Composable
private fun TarjetaCotizacion(cot: Cotizacion) {
    var abierta by remember { mutableStateOf(false) }
    TarjetaTs(Modifier.fillMaxWidth(), onClick = { abierta = !abierta }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "#${cot.folio}",
                style = MaterialTheme.typography.titleSmall,
                fontFamily = FuenteMono,
                fontWeight = FontWeight.Bold,
                color = TsSecundario,
                modifier = Modifier.weight(1f)
            )
            EstadoChip(cot.estado)
        }
        Text(
            buildString {
                append(cot.fecha)
                val vence = cot.vencimiento.substringBefore(" (")
                if (vence.isNotBlank()) append(" · Vence: $vence")
            },
            style = MaterialTheme.typography.bodySmall,
            color = TsNeutro
        )
        Spacer(Modifier.height(8.dp))
        CajaSuave {
            if (abierta) {
                cot.items.forEach { FilaMonto("${it.cantidad} × ${it.nombreMostrado}", dinero(it.subtotalMostrado)) }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = TsBorde)
                FilaMonto("Subtotal neto", dinero(cot.subtotalNeto))
                if (cot.descuento > 0.0) FilaMonto("Ahorro por volumen", "−" + dinero(cot.descuento), colorValor = TsExitoTexto)
                FilaMonto("IVA (13%)", dinero(cot.iva))
            } else {
                Text(
                    resumenItems(cot),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TsSecundario,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(6.dp))
            FilaMonto("Total", dinero(cot.total), destacado = true)
        }
        Text(
            if (abierta) "Ocultar detalle" else "Toca para ver el detalle",
            style = MaterialTheme.typography.labelSmall,
            color = TsPrimario,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
private fun TarjetaPago(pago: Pago, onClick: () -> Unit) {
    val (icono: ImageVector, nombreMetodo) = when (pago.metodo) {
        Cotizacion.PAGO_TARJETA -> Icons.Outlined.CreditCard to "Tarjeta"
        Cotizacion.PAGO_TRANSFERENCIA -> Icons.Outlined.AccountBalance to "Transferencia"
        else -> Icons.Outlined.VerifiedUser to "Crédito Techsmart"
    }
    val aprobado = pago.estado == Pago.ESTADO_APROBADO

    TarjetaTs(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconoEnCaja(icono, tamano = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(pago.referencia.ifBlank { nombreMetodo }, style = MaterialTheme.typography.titleSmall, color = TsSecundario)
                Text(
                    "Pedido #${pago.folioPedido} · ${pago.fecha}" + if (pago.hora.isNotBlank()) " ${pago.hora}" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = TsNeutro,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    dinero(pago.monto),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TsSecundario
                )
                EtiquetaMono(
                    pago.estado,
                    fondo = if (aprobado) TsExitoFondo else TsAvisoFondo,
                    color = if (aprobado) TsExitoTexto else TsAvisoTexto
                )
            }
        }
        Text(
            "Comprobante ${pago.id.take(8).uppercase()} · IVA incluido ${dinero(pago.iva)}",
            style = MaterialTheme.typography.labelSmall,
            color = TsNeutro,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
