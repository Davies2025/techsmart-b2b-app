package com.example.techsmart.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.Producto
import com.example.techsmart.ui.components.*
import com.example.techsmart.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    unidadesEnLote: Int,
    subtotalLote: Double,
    onAgregarAlCarrito: (Producto, Int) -> Unit,
    onVerLote: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var busqueda by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = TsFondo,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (unidadesEnLote > 0) BarraLote(unidadesEnLote, subtotalLote, onVerLote)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ---------- Búsqueda ----------
            CampoTs(
                valor = busqueda,
                onCambio = { busqueda = it },
                placeholder = "Buscar por nombre o SKU",
                leadingIcon = Icons.Outlined.Search,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
            )

            when (val s = state) {
                is CatalogUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is CatalogUiState.Error -> MensajeVacio("No se pudo cargar el catálogo: ${s.message}")

                is CatalogUiState.Success -> {
                    val categorias = remember(s.productos) {
                        s.productos.map { it.categoria }.filter { it.isNotBlank() }.distinct().sorted()
                    }
                    val filtrados = remember(s.productos, busqueda, categoria) {
                        val q = busqueda.trim().lowercase()
                        s.productos
                            .filter { categoria == null || it.categoria == categoria }
                            .filter {
                                q.isEmpty() || it.nombre.lowercase().contains(q) ||
                                    it.sku.lowercase().contains(q)
                            }
                            .sortedWith(compareByDescending<Producto> { it.destacado }.thenBy { it.nombre })
                    }

                    // ---------- Categorías ----------
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            ChipCategoria("Todos", s.productos.size, categoria == null) { categoria = null }
                        }
                        items(categorias) { c ->
                            ChipCategoria(c, null, categoria == c) {
                                categoria = if (categoria == c) null else c
                            }
                        }
                    }

                    // ---------- Encabezado ----------
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            categoria ?: "Catálogo Directo",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TsSecundario,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${filtrados.size} productos",
                            style = MaterialTheme.typography.labelMedium,
                            color = TsNeutro
                        )
                    }

                    if (filtrados.isEmpty()) {
                        MensajeVacio(
                            if (s.productos.isEmpty()) "No hay productos disponibles."
                            else "Ningún producto coincide con la búsqueda."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(filtrados, key = { it.id }) { producto ->
                                TarjetaProducto(producto) { cantidad ->
                                    onAgregarAlCarrito(producto, cantidad)
                                    scope.launch {
                                        snackbar.currentSnackbarData?.dismiss()
                                        snackbar.showSnackbar("$cantidad × ${producto.nombre} agregado al lote")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipCategoria(texto: String, conteo: Int?, activo: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (activo) TsPrimario else TsSuperficie,
        border = if (activo) null else androidx.compose.foundation.BorderStroke(1.dp, TsBorde)
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                texto,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = if (activo) Color.White else TsSecundario
            )
            if (conteo != null) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    color = if (activo) Color.White.copy(alpha = 0.25f) else TsAzulSuave,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "$conteo",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (activo) Color.White else TsPrimario,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaProducto(producto: Producto, onAgregar: (Int) -> Unit) {
    var cantidad by remember { mutableIntStateOf(1) }
    // Tramos con descuento (el de 1 unidad repite el precio base)
    val tramos = producto.precioVolumen
        .filter { it.precioEfectivo > 0.0 && it.minQty > 1 && it.precioEfectivo < producto.precio }
        .sortedBy { it.minQty }
    val (textoStock, fondoStock, colorStock) = estadoStock(producto)

    TarjetaTs(Modifier.fillMaxWidth()) {
        // ---------- Etiquetas y SKU ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            FlowRow(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (producto.categoria.isNotBlank()) {
                    EtiquetaMono(producto.categoria, fondo = TsSuperficieSuave, color = TsSecundario)
                }
                Surface(color = fondoStock, shape = RoundedCornerShape(50)) {
                    Row(
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(colorStock))
                        Spacer(Modifier.width(4.dp))
                        Text(textoStock, style = MaterialTheme.typography.labelSmall, color = colorStock)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                producto.sku,
                style = MaterialTheme.typography.labelSmall,
                color = TsNeutro,
                maxLines = 1
            )
        }

        Spacer(Modifier.height(12.dp))

        // ---------- Imagen y nombre ----------
        Row(verticalAlignment = Alignment.Top) {
            ImagenProducto(producto.imagen)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    color = TsSecundario,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (producto.descripcionCorta.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        producto.descripcionCorta,
                        style = MaterialTheme.typography.bodySmall,
                        color = TsNeutro,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    dinero(producto.precio),
                    style = MaterialTheme.typography.titleLarge,
                    color = TsPrimario,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // ---------- Especificaciones ----------
        val specs = producto.specs.filter { it.label.isNotBlank() && it.value.isNotBlank() }.take(2)
        if (specs.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TsSuperficieSuave)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                specs.forEach { spec ->
                    Row {
                        Text(
                            "${spec.label}: ",
                            style = MaterialTheme.typography.bodySmall,
                            color = TsNeutro
                        )
                        Text(
                            spec.value,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TsSecundario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // ---------- Precios por volumen ----------
        if (tramos.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Mayorista: " + tramos.joinToString("  ·  ") { "${it.minQty}+ ${dinero(it.precioEfectivo)}" },
                style = MaterialTheme.typography.labelSmall,
                color = TsPrimario,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(12.dp))

        // ---------- Cantidad y agregar ----------
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TsSuperficieSuave),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (cantidad > 1) cantidad-- }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = "Menos", tint = TsSecundario)
                }
                Text(
                    "$cantidad",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(onClick = { cantidad++ }, modifier = Modifier.size(44.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Más", tint = TsSecundario)
                }
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = { onAgregar(cantidad); cantidad = 1 },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Agregar al Lote", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Texto y colores del indicador de stock. */
private fun estadoStock(p: Producto): Triple<String, Color, Color> = when {
    p.disponibilidad == "bajo_pedido" -> Triple("Bajo pedido", TsAvisoFondo, TsAvisoTexto)
    p.stock <= 0 -> Triple("Sin stock", TsErrorFondo, TsErrorTexto)
    p.stock <= 5 -> Triple("Últimas ${p.stock} unid.", TsErrorFondo, TsErrorTexto)
    p.disponibilidad == "48h" -> Triple("Entrega 48 h (${p.stock}u)", TsInfoFondo, TsInfoTexto)
    else -> Triple("Stock inmediato (${p.stock}u)", Color(0xFFD1FAE5), Color(0xFF0E7C66))
}

/** Barra inferior "Mi Lote de Cotización" con unidades, subtotal y acceso a Cotizaciones. */
@Composable
private fun BarraLote(unidades: Int, subtotal: Double, onVerLote: () -> Unit) {
    Surface(color = TsSuperficie, shadowElevation = 8.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(badge = { Badge { Text("$unidades") } }) {
                IconoEnCaja(Icons.Outlined.Inventory2, tamano = 44.dp, fondo = TsPrimario, tinte = Color.White)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Mi Lote de Cotización", style = MaterialTheme.typography.titleSmall, color = TsSecundario)
                Text(
                    "Subtotal: ${dinero(subtotal)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TsNeutro
                )
            }
            Button(onClick = onVerLote, shape = RoundedCornerShape(12.dp)) {
                Text("Ver Lote")
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
            }
        }
    }
}
