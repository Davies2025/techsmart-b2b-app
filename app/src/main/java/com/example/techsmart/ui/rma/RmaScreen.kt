package com.example.techsmart.ui.rma

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.TicketRMA
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.ui.components.EstadoChip
import com.example.techsmart.ui.components.MensajeVacio

@Composable
fun RmaScreen(viewModel: RmaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val snackbar = remember { SnackbarHostState() }
    // Se incrementa tras cada envío exitoso para limpiar el formulario
    var formularioVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
        viewModel.observarMisTickets()
    }
    LaunchedEffect(state.folioCreado) {
        state.folioCreado?.let { folio ->
            formularioVersion++
            pestana = 1
            viewModel.consumirTicketCreado()
            snackbar.showSnackbar("Reclamo $folio registrado.")
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
                Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = { Text("Nuevo reclamo") })
                Tab(
                    selected = pestana == 1,
                    onClick = { pestana = 1 },
                    text = { Text("Mis reclamos (${state.tickets.size})") }
                )
            }
            when (pestana) {
                0 -> key(formularioVersion) {
                    FormularioReclamo(
                        perfil = state.perfil,
                        enviando = state.isSubmitting,
                        onEnviar = viewModel::crearTicketRMA
                    )
                }
                else -> MisReclamos(state)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pestaña 1: formulario
// ---------------------------------------------------------------------------

private fun contactoDesdePerfil(perfil: Usuario?): DatosReclamo =
    if (perfil == null) DatosReclamo()
    else DatosReclamo(
        cliente = perfil.razonSocial.ifBlank { perfil.nombreContacto },
        contacto = perfil.nombreContacto,
        telefono = perfil.telefono,
        correo = perfil.correo
    )

@Composable
private fun FormularioReclamo(
    perfil: Usuario?,
    enviando: Boolean,
    onEnviar: (DatosReclamo) -> Unit
) {
    var datos by remember(perfil) { mutableStateOf(contactoDesdePerfil(perfil)) }
    val incompleto = datos.productoNombre.isBlank() || datos.numeroSerie.isBlank() ||
        datos.facturaReferencia.isBlank() || datos.descripcionFalla.isBlank() ||
        datos.cliente.isBlank() || datos.contacto.isBlank() || datos.correo.isBlank()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Equipo", style = MaterialTheme.typography.titleMedium)
        Campo("Producto", datos.productoNombre) { datos = datos.copy(productoNombre = it) }
        Campo("SKU (opcional)", datos.sku) { datos = datos.copy(sku = it) }
        Campo("Número de serie", datos.numeroSerie) { datos = datos.copy(numeroSerie = it) }
        Campo("Factura / CCF de referencia", datos.facturaReferencia) {
            datos = datos.copy(facturaReferencia = it)
        }
        OutlinedTextField(
            value = datos.descripcionFalla,
            onValueChange = { datos = datos.copy(descripcionFalla = it) },
            label = { Text("Descripción de la falla") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Falla de fábrica (DOA)", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Marca esta opción si el equipo falló en los primeros 7 días. " +
                            "El cambio se aprueba de inmediato.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = datos.esDOA, onCheckedChange = { datos = datos.copy(esDOA = it) })
            }
        }

        Text("Contacto", style = MaterialTheme.typography.titleMedium)
        Campo("Cliente / razón social", datos.cliente) { datos = datos.copy(cliente = it) }
        Campo("Persona de contacto", datos.contacto) { datos = datos.copy(contacto = it) }
        Campo("Teléfono", datos.telefono, KeyboardType.Phone) { datos = datos.copy(telefono = it) }
        Campo("Correo de contacto", datos.correo, KeyboardType.Email) { datos = datos.copy(correo = it) }

        Button(
            onClick = { onEnviar(datos) },
            enabled = !enviando && !incompleto,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            if (enviando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text("Registrar reclamo")
        }
        if (incompleto) {
            Text(
                "Completa los campos obligatorios para enviar.",
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

// ---------------------------------------------------------------------------
// Pestaña 2: historial (incluye los creados en la web)
// ---------------------------------------------------------------------------

@Composable
private fun MisReclamos(state: RmaUiState) {
    when {
        state.cargandoTickets && state.tickets.isEmpty() -> Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        state.tickets.isEmpty() -> MensajeVacio("Aún no tienes reclamos registrados.")

        else -> LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.tickets, key = { it.id.ifBlank { it.folio } }) { TarjetaTicket(it) }
        }
    }
}

@Composable
private fun TarjetaTicket(t: TicketRMA) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    t.folio,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f)
                )
                EstadoChip(t.estado)
            }
            Text(
                t.productoNombre.ifBlank { t.sku },
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                "Serie ${t.numeroSerie} · Factura ${t.facturaReferencia} · ${t.fecha}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (t.esDOA) {
                Text(
                    "Garantía DOA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Text(t.descripcionFalla, style = MaterialTheme.typography.bodySmall)
            if (t.diagnosticoTecnico.isNotBlank()) {
                Text(
                    "Diagnóstico: ${t.diagnosticoTecnico}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (t.resolucion.isNotBlank()) {
                Text(
                    "Resolución: ${t.resolucion}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
