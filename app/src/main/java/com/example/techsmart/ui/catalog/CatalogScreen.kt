package com.example.techsmart.ui.catalog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.Producto
import com.example.techsmart.ui.components.MensajeVacio
import com.example.techsmart.ui.components.dinero
import kotlinx.coroutines.launch

@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onAgregarAlCarrito: (Producto, Int) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var busqueda by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                "Catálogo",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text("Buscar por nombre o SKU") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
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

                    if (categorias.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = categoria == null,
                                onClick = { categoria = null },
                                label = { Text("Todas") }
                            )
                            categorias.forEach { c ->
                                FilterChip(
                                    selected = categoria == c,
                                    onClick = { categoria = if (categoria == c) null else c },
                                    label = { Text(c) }
                                )
                            }
                        }
                    }

                    if (filtrados.isEmpty()) {
                        MensajeVacio(
                            if (s.productos.isEmpty()) "No hay productos disponibles."
                            else "Ningún producto coincide con la búsqueda."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(filtrados, key = { it.id }) { producto ->
                                TarjetaProducto(producto) { cantidad ->
                                    onAgregarAlCarrito(producto, cantidad)
                                    scope.launch {
                                        snackbar.currentSnackbarData?.dismiss()
                                        snackbar.showSnackbar("$cantidad × ${producto.nombre} agregado a la cotización")
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
private fun TarjetaProducto(producto: Producto, onAgregar: (Int) -> Unit) {
    var cantidad by remember { mutableIntStateOf(1) }
    val tramos = producto.precioVolumen
        .filter { it.precioEfectivo > 0.0 }
        .sortedBy { it.minQty }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(producto.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                "SKU ${producto.sku}" + if (producto.categoria.isNotBlank()) " · ${producto.categoria}" else "",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (producto.descripcionCorta.isNotBlank()) {
                Text(
                    producto.descripcionCorta,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dinero(producto.precio),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    textoDisponibilidad(producto),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (producto.stock > 0) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error
                )
            }

            if (tramos.isNotEmpty()) {
                Text(
                    tramos.joinToString("  ·  ") { "${it.minQty}+ u: ${dinero(it.precioEfectivo)}" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (cantidad > 1) cantidad-- }) {
                    Icon(Icons.Default.Remove, contentDescription = "Menos")
                }
                Text("$cantidad", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { cantidad++ }) {
                    Icon(Icons.Default.Add, contentDescription = "Más")
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { onAgregar(cantidad); cantidad = 1 }) {
                    Text("Agregar")
                }
            }
        }
    }
}

private fun textoDisponibilidad(p: Producto): String = when (p.disponibilidad) {
    "48h" -> "Entrega 48 h · stock ${p.stock}"
    "bajo_pedido" -> "Bajo pedido"
    else -> if (p.stock > 0) "En stock: ${p.stock}" else "Sin stock"
}
