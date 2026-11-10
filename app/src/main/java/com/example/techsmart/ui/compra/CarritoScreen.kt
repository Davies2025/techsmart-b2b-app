package com.example.techsmart.ui.compra

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCartCheckout
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.ItemCotizacion
import com.example.techsmart.ui.cart.CartUiState
import com.example.techsmart.ui.cart.CartViewModel
import com.example.techsmart.ui.components.*
import com.example.techsmart.ui.theme.*

/** Carrito de compras: el mismo lote de la cotización, pero para comprar en firme. */
@Composable
fun CarritoScreen(
    viewModel: CartViewModel,
    onVolver: () -> Unit,
    onIrACatalogo: () -> Unit,
    onContinuarPago: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val unidades = state.items.sumOf { it.cantidad }

    val azulReal = Color(0xFF155EEF)
    val verdeEsmeralda = Color(0xFF0A876C)

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (state.items.isNotEmpty()) {
                BotonContinuar(
                    total = state.totales.total,
                    ahorro = state.totales.descuento,
                    onClick = onContinuarPago
                )
            }
        }
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
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(verdeEsmeralda.copy(alpha = 0.07f), Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.85f),
                        radius = size.width * 0.5f
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.85f),
                    radius = size.width * 0.5f
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    FilaVolver("Volver", onVolver) {
                        if (state.items.isNotEmpty()) {
                            Surface(
                                color = TsPrimario.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.22f))
                            ) {
                                Text(
                                    text = "${state.items.size} ${if (state.items.size == 1) "ARTÍCULO" else "ARTÍCULOS"} · $unidades U.",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FuenteMono,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TsPrimario,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    HeaderCarritoCompra(unidades = unidades)
                }

                if (state.items.isEmpty()) {
                    item {
                        EstadoCarritoVacio(onIrACatalogo = onIrACatalogo)
                    }
                } else {
                    item {
                        EncabezadoArticulos(
                            totalArticulos = state.items.size,
                            onIrACatalogo = onIrACatalogo
                        )
                    }

                    items(state.items, key = { it.productoId }) { item ->
                        TarjetaArticulo(
                            item = item,
                            onCantidad = { viewModel.cambiarCantidad(item.productoId, it) },
                            onEliminar = { viewModel.eliminarItem(item.productoId) }
                        )
                    }

                    item {
                        Desglose(state)
                    }

                    item {
                        SellosConfianzaCompra()
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCarritoCompra(unidades: Int) {
    val azulNoche = Color(0xFF0A1931)
    val azulReal = Color(0xFF155EEF)
    val cianPulso = Color(0xFF00D2FF)
    val verdeMenta = Color(0xFF34D399)

    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.35f)),
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
                val cy = size.height * 0.22f
                drawCircle(
                    color = cianPulso.copy(alpha = 0.14f),
                    radius = size.width * 0.28f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = size.width * 0.18f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Column(modifier = Modifier.padding(18.dp)) {
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
                                imageVector = Icons.Outlined.ShoppingCartCheckout,
                                contentDescription = null,
                                tint = cianPulso,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "COMPRA DIRECTA TECHSMART",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = cianPulso
                        )
                        Text(
                            text = "Carrito de Compras",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Compra en firme con precios por volumen automáticos e IVA 13% incluido.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.24f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PasoFlujoCompra("1", "Carrito", activo = true, colorActivo = verdeMenta)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .height(1.5.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        )
                        PasoFlujoCompra("2", "Facturación y Pago", activo = false, colorActivo = cianPulso)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .height(1.5.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        )
                        PasoFlujoCompra("3", "Recibo", activo = false, colorActivo = cianPulso)

                    }
                }
            }
        }
    }
}

@Composable
private fun PasoFlujoCompra(
    numero: String,
    titulo: String,
    activo: Boolean,
    colorActivo: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (activo) colorActivo else Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = numero,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontFamily = FuenteMono,
                fontWeight = FontWeight.ExtraBold,
                color = if (activo) Color(0xFF0A1931) else Color.White.copy(alpha = 0.75f)
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = if (activo) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (activo) Color.White else Color.White.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun EncabezadoArticulos(
    totalArticulos: Int,
    onIrACatalogo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            listOf(TsPrimario, Color(0xFF00C2FF))
                        )
                    )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Equipos Seleccionados ($totalArticulos)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TsSecundario
            )
        }

        Surface(
            onClick = onIrACatalogo,
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.25f))
        ) {
            Text(
                text = "+ Agregar",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TsPrimario,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun EstadoCarritoVacio(onIrACatalogo: () -> Unit) {
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.20f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF2F7FF), Color.White)
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = TsPrimario.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.22f)),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingBag,
                        contentDescription = null,
                        tint = TsPrimario,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Tu carrito está vacío",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TsSecundario
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Agrega equipos desde el catálogo para activar descuentos por volumen y procesar tu compra inmediata.",
                style = MaterialTheme.typography.bodySmall,
                color = TsNeutro,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = onIrACatalogo,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = verdeEsmeralda,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Outlined.Inventory2, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Ir al catálogo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TarjetaArticulo(
    item: ItemCotizacion,
    onCantidad: (Int) -> Unit,
    onEliminar: () -> Unit
) {
    val conDescuento = item.precioBase > item.precioUnitario
    val verdeEsmeralda = Color(0xFF0A876C)
    val ahorroItem = (item.precioBase - item.precioUnitario) * item.cantidad

    // Detector inteligente de descuentos por volumen
    val proximoTramo = item.producto.precioVolumen
        .filter { it.minQty > item.cantidad && it.precioEfectivo > 0.0 }
        .minByOrNull { it.minQty }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(
            1.2.dp,
            if (conDescuento) verdeEsmeralda.copy(alpha = 0.35f) else TsPrimario.copy(alpha = 0.18f)
        ),
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
                        if (conDescuento) {
                            Brush.horizontalGradient(listOf(verdeEsmeralda, Color(0xFF34D399)))
                        } else {
                            Brush.horizontalGradient(listOf(TsPrimario, Color(0xFF00C2FF)))
                        }
                    )
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.16f))
                    ) {
                        ImagenProducto(item.producto.imagen, 82.dp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = TsPrimario.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.skuMostrado,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FuenteMono,
                                    color = TsPrimario,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                onClick = onEliminar,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "Quitar",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = item.nombreMostrado,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsSecundario,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${dinero(item.precioUnitario)} c/u",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (conDescuento) verdeEsmeralda else TsNeutro
                            )
                            if (conDescuento) {
                                Text(
                                    text = dinero(item.precioBase),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TsNeutro,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (conDescuento) Color(0xFFF2FBF8) else Color(0xFFF6F9FF),
                    border = BorderStroke(
                        1.dp,
                        if (conDescuento) verdeEsmeralda.copy(alpha = 0.22f) else TsPrimario.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Subtotal partida",
                                style = MaterialTheme.typography.labelSmall,
                                color = TsNeutro
                            )
                            Text(
                                text = dinero(item.subtotalMostrado),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (conDescuento) verdeEsmeralda else TsPrimario
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 1.dp,
                            border = BorderStroke(1.dp, TsBorde)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onCantidad(item.cantidad - 1) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Menos",
                                        tint = TsSecundario,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "${item.cantidad}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontFamily = FuenteMono,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TsSecundario,
                                    modifier = Modifier.widthIn(min = 26.dp),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    onClick = { onCantidad(item.cantidad + 1) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Más",
                                        tint = TsPrimario,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                if (conDescuento && ahorroItem > 0.0) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = verdeEsmeralda,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "Precio por volumen activo · Ahorras ${dinero(ahorroItem)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = verdeEsmeralda
                        )
                    }
                }

                if (proximoTramo != null) {
                    val faltan = proximoTramo.minQty - item.cantidad
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCantidad(proximoTramo.minQty) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocalOffer,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Agrega $faltan más para bajar a ${dinero(proximoTramo.precioEfectivo)} c/u",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Aplicar",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TsPrimario
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Desglose(state: CartUiState) {
    val azulNoche = Color(0xFF0A1931)
    val cianPulso = Color(0xFF00D2FF)
    val verdeMenta = Color(0xFF34D399)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = azulNoche,
        shadowElevation = 6.dp,
        border = BorderStroke(1.2.dp, cianPulso.copy(alpha = 0.32f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = cianPulso,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Desglose de Compra",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "USD $",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FuenteMono,
                        fontWeight = FontWeight.ExtraBold,
                        color = cianPulso,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            FilaResumenOscuro("Subtotal neto", dinero(state.totales.subtotalNeto))

            if (state.totales.descuento > 0.0) {
                Spacer(Modifier.height(6.dp))
                FilaResumenOscuro(
                    etiqueta = "Ahorro por volumen",
                    valor = "−" + dinero(state.totales.descuento),
                    colorValor = verdeMenta
                )
            }

            Spacer(Modifier.height(6.dp))
            FilaResumenOscuro("IVA (13%)", dinero(state.totales.iva))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = Color.White.copy(alpha = 0.15f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Final",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Listo para facturar",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.70f)
                    )
                }
                Text(
                    text = dinero(state.totales.total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = cianPulso
                )
            }
        }
    }
}

@Composable
private fun FilaResumenOscuro(
    etiqueta: String,
    valor: String,
    colorValor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.78f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FuenteMono,
            fontWeight = FontWeight.Bold,
            color = colorValor
        )
    }
}

@Composable
private fun SellosConfianzaCompra() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PildoraConfianza(
            modifier = Modifier.weight(1f),
            icono = Icons.Default.Lock,
            titulo = "Paga Fácil",
            subtitulo = "Tarjetas"
        )
        PildoraConfianza(
            modifier = Modifier.weight(1f),
            icono = Icons.Outlined.VerifiedUser,
            titulo = "Garantía",
            subtitulo = "Respaldo RMA"
        )
    }
}

@Composable
private fun PildoraConfianza(
    modifier: Modifier = Modifier,
    icono: ImageVector,
    titulo: String,
    subtitulo: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.90f),
        border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = TsPrimario,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TsSecundario,
                maxLines = 1
            )
            Text(
                text = subtitulo,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = TsNeutro,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun BotonContinuar(
    total: Double,
    ahorro: Double,
    onClick: () -> Unit
) {
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        color = Color.White.copy(alpha = 0.95f),
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.12f))
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = verdeEsmeralda,
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(64.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.18f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lock, null, Modifier.size(18.dp), tint = Color.White)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Continuar al Pago",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (ahorro > 0.0) "Ahorrando ${dinero(ahorro)}"
                    else "Tarjeta o crédito",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.88f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                color = Color(0xFF0A1931).copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dinero(total),
                        style = MaterialTheme.typography.labelLarge,
                        fontFamily = FuenteMono,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
internal fun FilaVolver(texto: String, onVolver: () -> Unit, derecha: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            onClick = onVolver,
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.20f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = TsPrimario,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = texto,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TsPrimario
                )
            }
        }
        Spacer(Modifier.weight(1f))
        derecha()
    }
}