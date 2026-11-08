package com.example.techsmart.ui.compra

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    Scaffold(
        containerColor = TsFondo,
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (state.items.isNotEmpty()) BotonContinuar(state.totales.total, onContinuarPago)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FilaVolver("Volver", onVolver) {
                    if (state.items.isNotEmpty()) {
                        EtiquetaMono("${state.items.size} ${if (state.items.size == 1) "artículo" else "artículos"} · $unidades u.")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Carrito de Compras", style = MaterialTheme.typography.headlineMedium, color = TsSecundario)
                Text(
                    "Compra en firme con precios por volumen e IVA 13% incluido.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TsNeutro
                )
            }

            if (state.items.isEmpty()) {
                item {
                    TarjetaTs(Modifier.fillMaxWidth()) {
                        Text("Tu carrito está vacío", style = MaterialTheme.typography.titleSmall, color = TsSecundario)
                        Text(
                            "Agrega productos desde el catálogo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TsNeutro
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onIrACatalogo, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Inventory2, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ir al catálogo")
                        }
                    }
                }
            } else {
                items(state.items, key = { it.productoId }) { item ->
                    TarjetaArticulo(
                        item = item,
                        onCantidad = { viewModel.cambiarCantidad(item.productoId, it) },
                        onEliminar = { viewModel.eliminarItem(item.productoId) }
                    )
                }
                item { Desglose(state) }
            }
        }
    }
}

@Composable
private fun TarjetaArticulo(item: ItemCotizacion, onCantidad: (Int) -> Unit, onEliminar: () -> Unit) {
    TarjetaTs(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            ImagenProducto(item.producto.imagen, 84.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.skuMostrado,
                            style = MaterialTheme.typography.labelSmall,
                            color = TsPrimario,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            item.nombreMostrado,
                            style = MaterialTheme.typography.titleSmall,
                            color = TsSecundario,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.DeleteOutline, "Quitar", tint = TsNeutro)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val conDescuento = item.precioBase > item.precioUnitario
                    Text(
                        "${dinero(item.precioUnitario)} c/u" + if (conDescuento) " · volumen" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (conDescuento) TsExitoTexto else TsNeutro,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        dinero(item.subtotalMostrado),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TsPrimario
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    Modifier
                        .align(Alignment.End)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TsSuperficieSuave),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onCantidad(item.cantidad - 1) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Remove, "Menos", tint = TsSecundario)
                    }
                    Text("${item.cantidad}", style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { onCantidad(item.cantidad + 1) }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Add, "Más", tint = TsSecundario)
                    }
                }
            }
        }
    }
}

@Composable
private fun Desglose(state: CartUiState) {
    TarjetaTs(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Desglose de Compra",
                style = MaterialTheme.typography.titleMedium,
                color = TsSecundario,
                modifier = Modifier.weight(1f)
            )
            EtiquetaMono("USD $", fondo = TsSuperficieSuave, color = TsNeutro)
        }
        Spacer(Modifier.height(10.dp))
        FilaMonto("Subtotal neto", dinero(state.totales.subtotalNeto))
        if (state.totales.descuento > 0.0) {
            FilaMonto("Ahorro por volumen", "−" + dinero(state.totales.descuento), colorValor = TsExitoTexto)
        }
        FilaMonto("IVA (13%)", dinero(state.totales.iva))
        HorizontalDivider(Modifier.padding(vertical = 10.dp), color = TsBorde)
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text("Total Final", style = MaterialTheme.typography.titleLarge, color = TsSecundario)
                Text("IVA incluido", style = MaterialTheme.typography.labelSmall, color = TsNeutro)
            }
            Text(
                dinero(state.totales.total),
                style = MaterialTheme.typography.headlineMedium,
                color = TsPrimario
            )
        }
    }
}

@Composable
private fun BotonContinuar(total: Double, onClick: () -> Unit) {
    Surface(color = TsFondo) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(64.dp)
        ) {
            Icon(Icons.Default.Lock, null, Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Continuar al Pago", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Tarjeta, transferencia o crédito",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Surface(color = Color.White.copy(alpha = 0.18f), shape = RoundedCornerShape(10.dp)) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(dinero(total), style = MaterialTheme.typography.labelLarge, color = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp), tint = Color.White)
                }
            }
        }
    }
}

/** Fila superior con "← Volver" y contenido opcional a la derecha. */
@Composable
internal fun FilaVolver(texto: String, onVolver: () -> Unit, derecha: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onVolver, contentPadding = PaddingValues(horizontal = 4.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(texto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.weight(1f))
        derecha()
    }
}
