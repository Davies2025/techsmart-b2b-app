package com.example.techsmart.ui.cart

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.ItemCotizacion
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.ui.components.EstadoChip
import com.example.techsmart.ui.components.FilaMonto
import com.example.techsmart.ui.components.MensajeVacio
import com.example.techsmart.ui.components.dinero

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onIrACatalogo: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
        viewModel.observarMisCotizaciones()
    }

    LaunchedEffect(state.folioGuardado) {
        state.folioGuardado?.let { folio ->
            pestana = 1
            viewModel.consumirCotizacionGuardada()
            snackbar.showSnackbar("Cotización $folio enviada. Quedó en revisión.")
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            viewModel.consumirError()
            snackbar.showSnackbar(it)
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PrimaryTabRow(selectedTabIndex = pestana) {
                Tab(
                    selected = pestana == 0,
                    onClick = { pestana = 0 },
                    text = { Text("Nueva (${state.items.sumOf { it.cantidad }})") }
                )
                Tab(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    text = { Text("Mis cotizaciones") }
                )
            }
            when (pestana) {
                0 -> CotizacionActual(state, viewModel, onIrACatalogo)
                else -> MisCotizaciones(state)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pestaña 1: carrito + datos del cliente
// ---------------------------------------------------------------------------

private fun datosDesdePerfil(perfil: Usuario?): DatosCotizacion {
    if (perfil == null) return DatosCotizacion()
    return DatosCotizacion(
        tipoCliente = if (perfil.esEmpresa) Cotizacion.TIPO_EMPRESA else Cotizacion.TIPO_PARTICULAR,
        razonSocial = perfil.razonSocial.ifBlank { perfil.nombreContacto },
        nit = perfil.nit,
        contacto = perfil.nombreContacto,
        telefono = perfil.telefono,
        correo = perfil.correo
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CotizacionActual(
    state: CartUiState,
    viewModel: CartViewModel,
    onIrACatalogo: () -> Unit
) {
    if (state.items.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Tu cotización está vacía", style = MaterialTheme.typography.titleMedium)
            Text(
                "Agrega productos desde el catálogo para armarla.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onIrACatalogo) { Text("Ir al catálogo") }
        }
        return
    }

    // Se precarga con el perfil; luego el cliente puede editarlo
    var datos by remember(state.perfil) { mutableStateOf(datosDesdePerfil(state.perfil)) }
    val esEmpresa = datos.tipoCliente == Cotizacion.TIPO_EMPRESA
    val faltanDatos = datos.razonSocial.isBlank() || datos.contacto.isBlank() ||
        datos.correo.isBlank() || datos.telefono.isBlank() || (esEmpresa && datos.nit.isBlank())

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        state.items.forEach { item ->
            PartidaEditable(
                item = item,
                onCantidad = { viewModel.cambiarCantidad(item.productoId, it) },
                onEliminar = { viewModel.eliminarItem(item.productoId) }
            )
        }

        OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                FilaMonto("Subtotal neto", dinero(state.totales.subtotalNeto))
                if (state.totales.descuento > 0.0) {
                    FilaMonto(
                        "Ahorro por volumen",
                        "−" + dinero(state.totales.descuento),
                        colorValor = MaterialTheme.colorScheme.tertiary
                    )
                }
                FilaMonto("IVA (13%)", dinero(state.totales.iva))
                HorizontalDivider(Modifier.padding(vertical = 6.dp))
                FilaMonto("Total", dinero(state.totales.total), destacado = true)
            }
        }

        Text("Datos del cliente", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Cotizacion.TIPO_EMPRESA, Cotizacion.TIPO_PARTICULAR).forEach { tipo ->
                FilterChip(
                    selected = datos.tipoCliente == tipo,
                    onClick = { datos = datos.copy(tipoCliente = tipo) },
                    label = { Text(tipo) }
                )
            }
        }
        Campo(
            if (esEmpresa) "Razón social" else "Nombre completo",
            datos.razonSocial
        ) { datos = datos.copy(razonSocial = it) }
        Campo(
            if (esEmpresa) "NIT" else "DUI (opcional)",
            datos.nit
        ) { datos = datos.copy(nit = it) }
        Campo("Persona de contacto", datos.contacto) { datos = datos.copy(contacto = it) }
        Campo("Teléfono", datos.telefono, KeyboardType.Phone) { datos = datos.copy(telefono = it) }
        Campo("Correo de contacto", datos.correo, KeyboardType.Email) { datos = datos.copy(correo = it) }

        Text("Tiempo de entrega", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Cotizacion.TIEMPOS_ENTREGA.forEach { t ->
                FilterChip(
                    selected = datos.tiempoEntrega == t,
                    onClick = { datos = datos.copy(tiempoEntrega = t) },
                    label = { Text(t) }
                )
            }
        }
        Text("Validez de la oferta", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7, 15, 30).forEach { d ->
                FilterChip(
                    selected = datos.validezDias == d,
                    onClick = { datos = datos.copy(validezDias = d) },
                    label = { Text("$d días") }
                )
            }
        }
        OutlinedTextField(
            value = datos.notas,
            onValueChange = { datos = datos.copy(notas = it) },
            label = { Text("Notas (opcional)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { viewModel.guardarCotizacion(datos) },
            enabled = !state.isSubmitting && !faltanDatos,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Enviar cotización")
            }
        }
        if (faltanDatos) {
            Text(
                "Completa los datos del cliente para enviar.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    teclado: KeyboardType = KeyboardType.Text,
    onCambio: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PartidaEditable(
    item: ItemCotizacion,
    onCantidad: (Int) -> Unit,
    onEliminar: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(item.nombreMostrado, style = MaterialTheme.typography.titleSmall)
                    Text(
                        item.skuMostrado,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEliminar) {
                    Icon(Icons.Default.Delete, contentDescription = "Quitar")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onCantidad(item.cantidad - 1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Menos")
                }
                Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { onCantidad(item.cantidad + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Más")
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(dinero(item.subtotalMostrado), style = MaterialTheme.typography.titleSmall)
                    val conDescuento = item.precioBase > item.precioUnitario
                    Text(
                        "${dinero(item.precioUnitario)} c/u" + if (conDescuento) " (precio volumen)" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (conDescuento) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pestaña 2: historial (incluye las creadas en la web)
// ---------------------------------------------------------------------------

@Composable
private fun MisCotizaciones(state: CartUiState) {
    when {
        state.cargandoCotizaciones && state.misCotizaciones.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        state.misCotizaciones.isEmpty() -> MensajeVacio("Aún no tienes cotizaciones.")

        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.misCotizaciones, key = { it.id.ifBlank { it.folio } }) { cot ->
                TarjetaCotizacion(cot)
            }
        }
    }
}

@Composable
private fun TarjetaCotizacion(cot: Cotizacion) {
    var abierta by remember { mutableStateOf(false) }

    ElevatedCard(
        Modifier
            .fillMaxWidth()
            .clickable { abierta = !abierta }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    cot.folio,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(cot.estado)
            }
            Text(
                "${cot.fecha} · ${cot.items.size} partida(s)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (cot.vencimiento.isNotBlank()) {
                Text(
                    "Vence: ${cot.vencimiento}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilaMonto("Total", dinero(cot.total), destacado = true)

            if (abierta) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                cot.items.forEach { item ->
                    FilaMonto(
                        "${item.cantidad} × ${item.nombreMostrado}",
                        dinero(item.subtotalMostrado)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                FilaMonto("Subtotal neto", dinero(cot.subtotalNeto))
                if (cot.descuento > 0.0) FilaMonto("Ahorro por volumen", "−" + dinero(cot.descuento))
                FilaMonto("IVA (13%)", dinero(cot.iva))
                Spacer(Modifier.height(6.dp))
                Text(
                    "${cot.razonSocial} · ${cot.tiempoEntrega}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (cot.notas.isNotBlank()) {
                    Text("Notas: ${cot.notas}", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Text(
                    "Toca para ver el detalle",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
