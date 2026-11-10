package com.example.techsmart.ui.historial

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val azulReal = Color(0xFF155EEF)
    val verdeEsmeralda = Color(0xFF0A876C)

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
                    colors = listOf(azulReal.copy(alpha = 0.09f), Color.Transparent),
                    center = Offset(size.width * 0.90f, size.height * 0.08f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.90f, size.height * 0.08f),
                radius = size.width * 0.55f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(verdeEsmeralda.copy(alpha = 0.08f), Color.Transparent),
                    center = Offset(size.width * 0.10f, size.height * 0.88f),
                    radius = size.width * 0.55f
                ),
                center = Offset(size.width * 0.10f, size.height * 0.88f),
                radius = size.width * 0.55f
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                BovedaHistorialEjecutiva(
                    totalPagado = totalPagado,
                    totalPendiente = totalPendiente,
                    numCompras = compras.size,
                    numCotizaciones = cotizaciones.size,
                    numPagos = pagos.size
                )
            }

            item {
                SelectorPestanasHistorial(
                    seleccion = pestana,
                    conteoCompras = compras.size,
                    conteoCotizaciones = cotizaciones.size,
                    conteoPagos = pagos.size,
                    onSeleccion = { pestana = it }
                )
            }

            item {
                EncabezadoSeccionActiva(
                    pestana = pestana,
                    cantidad = when (pestana) {
                        TAB_COMPRAS -> compras.size
                        TAB_COTIZACIONES -> cotizaciones.size
                        else -> pagos.size
                    }
                )
            }

            if (state.cargandoCotizaciones && state.misCotizaciones.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TsPrimario)
                        }
                    }
                }
            } else when (pestana) {
                TAB_COMPRAS ->
                    if (compras.isEmpty()) {
                        item {
                            EstadoVacioHistorial(
                                icono = Icons.Outlined.ShoppingBag,
                                titulo = "Aún no tienes compras registradas",
                                subtitulo = "Tus órdenes confirmadas en firme y su estado de entrega en tiempo real aparecerán aquí."
                            )
                        }
                    } else {
                        items(compras, key = { "c-" + it.id.ifBlank { it.folio } }) { compra ->
                            TarjetaCompra(
                                compra = compra,
                                pago = pagos.find { it.pedidoId == compra.id || it.folioPedido == compra.folio }
                            ) {
                                onVerPedido(compra.folio)
                            }
                        }
                    }

                TAB_COTIZACIONES ->
                    if (cotizaciones.isEmpty()) {
                        item {
                            EstadoVacioHistorial(
                                icono = Icons.Outlined.RequestQuote,
                                titulo = "Aún no tienes cotizaciones",
                                subtitulo = "Guarda presupuestos formales desde la pestaña de Cotizaciones para consultarlos cuando quieras."
                            )
                        }
                    } else {
                        items(cotizaciones, key = { "q-" + it.id.ifBlank { it.folio } }) {
                            TarjetaCotizacion(it)
                        }
                    }

                else ->
                    if (pagos.isEmpty()) {
                        item {
                            EstadoVacioHistorial(
                                icono = Icons.Outlined.Payments,
                                titulo = "Aún no tienes pagos registrados",
                                subtitulo = "Aquí se listarán tus comprobantes digitales por tarjeta, transferencia o línea de crédito."
                            )
                        }
                    } else {
                        items(pagos, key = { "p-" + it.id }) { pago ->
                            TarjetaPago(pago) { onVerPedido(pago.folioPedido) }
                        }
                    }
            }
        }
    }
}

@Composable
private fun BovedaHistorialEjecutiva(
    totalPagado: Double,
    totalPendiente: Double,
    numCompras: Int,
    numCotizaciones: Int,
    numPagos: Int
) {
    val azulNoche = Color(0xFF0A1931)
    val azulReal = Color(0xFF155EEF)
    val cianPulso = Color(0xFF00D2FF)
    val verdeMenta = Color(0xFF34D399)
    val ambarAviso = Color(0xFFFBBF24)

    Surface(
        shape = RoundedCornerShape(26.dp),
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
                val cx = size.width * 0.90f
                val cy = size.height * 0.20f
                drawCircle(
                    color = cianPulso.copy(alpha = 0.14f),
                    radius = size.width * 0.34f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.20f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Column(modifier = Modifier.padding(20.dp)) {
                // Encabezado del banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cianPulso.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.45f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = cianPulso,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "REGISTRO DE TECHSMART",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FuenteMono,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = cianPulso
                        )
                        Text(
                            text = "Mi Historial",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    Surface(
                        color = verdeMenta.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, verdeMenta.copy(alpha = 0.40f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(verdeMenta)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "ACTIVO",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FuenteMono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.Black.copy(alpha = 0.26f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "TOTAL PAGADO",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FuenteMono,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.72f)
                            )
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = dinero(totalPagado),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = verdeMenta
                                )
                                Text(
                                    text = " USD",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = verdeMenta.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, verdeMenta.copy(alpha = 0.35f)),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.VerifiedUser,
                                    contentDescription = null,
                                    tint = verdeMenta,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricaHistorial(
                        icono = Icons.Outlined.ShoppingBag,
                        titulo = "Compras",
                        valor = "$numCompras",
                        colorAcento = cianPulso,
                        modifier = Modifier.weight(1f)
                    )
                    MetricaHistorial(
                        icono = Icons.Outlined.RequestQuote,
                        titulo = "Cotizaciones",
                        valor = "$numCotizaciones",
                        colorAcento = verdeMenta,
                        modifier = Modifier.weight(1f)
                    )
                    MetricaHistorial(
                        icono = Icons.Outlined.Payments,
                        titulo = "Pagos",
                        valor = "$numPagos",
                        colorAcento = cianPulso,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (totalPendiente > 0.0) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ambarAviso.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, ambarAviso.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.HourglassTop,
                                contentDescription = null,
                                tint = ambarAviso,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Pendiente de confirmar por transferencia: ${dinero(totalPendiente)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricaHistorial(
    icono: ImageVector,
    titulo: String,
    valor: String,
    colorAcento: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorAcento,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = valor,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FuenteMono,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.80f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SelectorPestanasHistorial(
    seleccion: String,
    conteoCompras: Int,
    conteoCotizaciones: Int,
    conteoPagos: Int,
    onSeleccion: (String) -> Unit
) {
    val pestanas = listOf(
        Triple(TAB_COMPRAS, Icons.Outlined.ShoppingBag, conteoCompras),
        Triple(TAB_COTIZACIONES, Icons.Outlined.RequestQuote, conteoCotizaciones),
        Triple(TAB_PAGOS, Icons.Outlined.Payments, conteoPagos)
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            pestanas.forEach { (nombre, icono, conteo) ->
                val activa = seleccion == nombre
                Surface(
                    onClick = { onSeleccion(nombre) },
                    shape = RoundedCornerShape(14.dp),
                    color = if (activa) TsPrimario else Color.Transparent,
                    shadowElevation = if (activa) 3.dp else 0.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = if (activa) Color.White else TsNeutro,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (activa) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (activa) Color.White else TsSecundario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (activa) Color.White.copy(alpha = 0.22f) else TsSuperficieSuave
                        ) {
                            Text(
                                text = "$conteo",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FuenteMono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (activa) Color.White else TsNeutro,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoSeccionActiva(pestana: String, cantidad: Int) {
    val verdeEsmeralda = Color(0xFF0A876C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        listOf(TsPrimario, verdeEsmeralda)
                    )
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = when (pestana) {
                TAB_COMPRAS -> "Órdenes de Compra"
                TAB_COTIZACIONES -> "Presupuestos Guardados"
                else -> "Comprobantes de Pago"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = TsSecundario,
            modifier = Modifier.weight(1f)
        )
        Surface(
            color = TsPrimario.copy(alpha = 0.10f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.20f))
        ) {
            Text(
                text = "$cantidad ${if (cantidad == 1) "REGISTRO" else "REGISTROS"}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FuenteMono,
                fontWeight = FontWeight.ExtraBold,
                color = TsPrimario,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EstadoVacioHistorial(
    icono: ImageVector,
    titulo: String,
    subtitulo: String
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.18f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF4F8FF), Color.White)
                    )
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = TsPrimario.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.22f)),
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = TsPrimario,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TsSecundario,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = TsNeutro,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

private fun resumenItems(c: Cotizacion): String =
    c.items.joinToString(" + ") { "${it.cantidad}x ${it.nombreMostrado}" }

@Composable
private fun TarjetaCompra(compra: Cotizacion, pago: Pago?, onClick: () -> Unit) {
    val verdeEsmeralda = Color(0xFF0A876C)
    val cianPulso = Color(0xFF00C2FF)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.20f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        Brush.horizontalGradient(listOf(TsPrimario, cianPulso, verdeEsmeralda))
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = TsPrimario.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "#${compra.folio}",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FuenteMono,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsPrimario,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    EstadoChip(compra.estado)
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = TsNeutro,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${compra.fecha} · ${compra.pagoReferencia}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TsNeutro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(10.dp))

                CajaSuave {
                    Text(
                        text = resumenItems(compra),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TsSecundario,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = TsBorde)
                    Spacer(Modifier.height(6.dp))
                    FilaMonto("Total facturado", dinero(compra.total), destacado = true)
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (pago?.estado) {
                            Pago.ESTADO_APROBADO -> Icons.Outlined.CheckCircle
                            Pago.ESTADO_PENDIENTE -> Icons.Outlined.HourglassTop
                            else -> Icons.AutoMirrored.Outlined.ReceiptLong
                        },
                        contentDescription = null,
                        tint = when (pago?.estado) {
                            Pago.ESTADO_APROBADO -> TsExitoTexto
                            Pago.ESTADO_PENDIENTE -> TsAvisoTexto
                            else -> TsNeutro
                        },
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = when (pago?.estado) {
                            Pago.ESTADO_APROBADO -> "Pago registrado"
                            Pago.ESTADO_PENDIENTE -> "Pago pendiente de confirmar"
                            else -> "Sin comprobante de pago"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (pago?.estado) {
                            Pago.ESTADO_APROBADO -> TsExitoTexto
                            Pago.ESTADO_PENDIENTE -> TsAvisoTexto
                            else -> TsNeutro
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = TsPrimario.copy(alpha = 0.10f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Seguimiento",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TsPrimario
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
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
private fun TarjetaCotizacion(cot: Cotizacion) {
    var abierta by remember { mutableStateOf(false) }

    Surface(
        onClick = { abierta = !abierta },
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.20f)),
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
                        Brush.horizontalGradient(listOf(TsPrimario, Color(0xFF00C2FF)))
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = TsPrimario.copy(alpha = 0.10f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "#${cot.folio}",
                            style = MaterialTheme.typography.labelLarge,
                            fontFamily = FuenteMono,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsPrimario,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    EstadoChip(cot.estado)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = buildString {
                        append(cot.fecha)
                        val vence = cot.vencimiento.substringBefore(" (")
                        if (vence.isNotBlank()) append(" · Vence: $vence")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TsNeutro
                )

                Spacer(Modifier.height(10.dp))

                CajaSuave {
                    if (abierta) {
                        cot.items.forEach {
                            FilaMonto("${it.cantidad} × ${it.nombreMostrado}", dinero(it.subtotalMostrado))
                        }
                        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = TsBorde)
                        FilaMonto("Subtotal neto", dinero(cot.subtotalNeto))
                        if (cot.descuento > 0.0) {
                            FilaMonto("Ahorro por volumen", "−" + dinero(cot.descuento), colorValor = TsExitoTexto)
                        }
                        FilaMonto("IVA (13%)", dinero(cot.iva))
                    } else {
                        Text(
                            text = resumenItems(cot),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TsSecundario,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = TsBorde)
                    Spacer(Modifier.height(6.dp))
                    FilaMonto("Total cotizado", dinero(cot.total), destacado = true)
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (abierta) "Ocultar desglose" else "Ver desglose completo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TsPrimario
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = if (abierta) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TsPrimario,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
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
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(
            1.2.dp,
            if (aprobado) verdeEsmeralda.copy(alpha = 0.28f) else TsPrimario.copy(alpha = 0.20f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoEnCaja(icono, tamano = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = pago.referencia.ifBlank { nombreMetodo },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TsSecundario
                    )
                    Text(
                        text = "Pedido #${pago.folioPedido} · ${pago.fecha}" + if (pago.hora.isNotBlank()) " ${pago.hora}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TsNeutro,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = dinero(pago.monto),
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FuenteMono,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (aprobado) verdeEsmeralda else TsSecundario
                    )
                    Spacer(Modifier.height(2.dp))
                    EtiquetaMono(
                        pago.estado,
                        fondo = if (aprobado) TsExitoFondo else TsAvisoFondo,
                        color = if (aprobado) TsExitoTexto else TsAvisoTexto
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = TsBorde)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Comprobante ${pago.id.take(8).uppercase()} · IVA incluido ${dinero(pago.iva)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FuenteMono,
                    color = TsNeutro
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TsPrimario,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}