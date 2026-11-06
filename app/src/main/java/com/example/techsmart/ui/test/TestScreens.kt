package com.example.techsmart.ui.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.techsmart.data.model.Producto
import com.example.techsmart.ui.cart.CartViewModel
import com.example.techsmart.ui.catalog.CatalogUiState
import com.example.techsmart.ui.catalog.CatalogViewModel
import com.example.techsmart.ui.rma.RmaViewModel

@Composable
fun InicioTestScreen(
    onIrACatalogo: () -> Unit,
    onIrACotizaciones: () -> Unit,
    onIrAReclamos: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pantalla de Prueba: INICIO", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onIrACatalogo, modifier = Modifier.fillMaxWidth()) { Text("Ir a Catálogo") }
        Button(onClick = onIrACotizaciones, modifier = Modifier.fillMaxWidth()) { Text("Ir a Cotizaciones") }
        Button(onClick = onIrAReclamos, modifier = Modifier.fillMaxWidth()) { Text("Ir a Reclamos / RMA") }
    }
}

@Composable
fun CatalogTestScreen(
    viewModel: CatalogViewModel,
    onAgregarAlCarrito: (Producto, Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pantalla de Prueba: CATÁLOGO (Firestore Flow)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        when (val s = state) {
            is CatalogUiState.Loading -> CircularProgressIndicator()
            is CatalogUiState.Error -> Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            is CatalogUiState.Success -> {
                if (s.productos.isEmpty()) {
                    Text("No hay productos en Firestore.")
                } else {
                    LazyColumn {
                        items(s.productos) { prod ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.nombre, style = MaterialTheme.typography.titleSmall)
                                        Text("SKU: ${prod.sku} | Base: $${prod.precio}")
                                    }
                                    Button(onClick = { onAgregarAlCarrito(prod, 1) }) {
                                        Text("+1")
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
fun CartTestScreen(viewModel: CartViewModel) {
    val state by viewModel.uiState.collectAsState()
    var razonSocial by remember { mutableStateOf("Empresa Demo S.A.") }
    var nit by remember { mutableStateOf("0614-010190-101-1") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pantalla de Prueba: COTIZADOR & IVA (13%)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Text("Ítems en Cotización: ${state.items.size}")
        Text("Subtotal Neto: $${String.format("%.2f", state.totales.subtotalNeto)}")
        Text("IVA (13%): $${String.format("%.2f", state.totales.iva)}")
        Text("Total Final: $${String.format("%.2f", state.totales.total)}")

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = razonSocial,
            onValueChange = { razonSocial = it },
            label = { Text("Razón Social") }
        )
        OutlinedTextField(
            value = nit,
            onValueChange = { nit = it },
            label = { Text("NIT") }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                viewModel.guardarCotizacion(
                    razonSocial = razonSocial,
                    nit = nit,
                    contacto = "Juan Pérez",
                    correo = "cliente@demo.com"
                )
            },
            enabled = state.items.isNotEmpty() && !state.isSubmitting
        ) {
            Text(if (state.isSubmitting) "Guardando en Firestore..." else "Guardar Cotización en Firestore")
        }

        state.cotizacionGuardadaId?.let { id ->
            Text("¡Éxito! Guardado con ID/Folio: $id", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RmaTestScreen(viewModel: RmaViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Pantalla de Prueba: RMA / DOA", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.crearTicketRMA(
                    sku = "SKU-RAM-16GB",
                    numeroSerie = "SN-9823411",
                    factura = "FAC-2026-001",
                    cliente = "Distribuidora Sigma",
                    descripcionFalla = "Falla de encendido (Módulo no detectado)",
                    esDOA = true
                )
            },
            enabled = !state.isSubmitting
        ) {
            Text(if (state.isSubmitting) "Creando..." else "Probar Creación de Ticket RMA (Falla DOA)")
        }

        state.ticketCreadoId?.let { id ->
            Text("¡Ticket RMA Creado! ID: $id", color = MaterialTheme.colorScheme.primary)
        }
    }
}