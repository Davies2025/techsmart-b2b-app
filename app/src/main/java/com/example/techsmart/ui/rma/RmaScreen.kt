package com.example.techsmart.ui.rma

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.TicketRMA
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.ui.components.*
import com.example.techsmart.ui.theme.*

// Posición del encabezado "Mis Casos" dentro de la lista (banner + botón/formulario)
private const val INDICE_CASOS = 2

@Composable
fun RmaScreen(viewModel: RmaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val lista = rememberLazyListState()
    var formularioAbierto by remember { mutableStateOf(false) }
    // Se incrementa tras cada envío exitoso para limpiar el formulario
    var formularioVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
        viewModel.observarMisTickets()
    }
    LaunchedEffect(state.folioCreado) {
        state.folioCreado?.let { folio ->
            formularioVersion++
            formularioAbierto = false
            viewModel.consumirTicketCreado()
            lista.animateScrollToItem(INDICE_CASOS)
            snackbar.showSnackbar("Reclamo $folio registrado.")
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            viewModel.consumirError()
            snackbar.showSnackbar(it)
        }
    }

    val activos = state.tickets.count { it.estado != "Cerrado" }

    Scaffold(
        containerColor = TsFondo,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            state = lista,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                BannerSeccion(
                    icono = Icons.Outlined.VerifiedUser,
                    titulo = "Garantía Techsmart Oficial",
                    descripcion = "Reporta fallas de tus equipos y da seguimiento a cada caso. " +
                        "Si el equipo falló en los primeros 7 días (DOA), el cambio se aprueba de inmediato."
                )
            }
            item {
                if (formularioAbierto) {
                    key(formularioVersion) {
                        FormularioReclamo(
                            perfil = state.perfil,
                            enviando = state.isSubmitting,
                            onEnviar = viewModel::crearTicketRMA,
                            onCerrar = { formularioAbierto = false }
                        )
                    }
                } else {
                    Button(
                        onClick = { formularioAbierto = true },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(60.dp)
                    ) {
                        Icon(Icons.Outlined.AddCircleOutline, null)
                        Spacer(Modifier.width(10.dp))
                        Text("Registrar Nuevo Reclamo / Garantía", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            item {
                EncabezadoSeccion(
                    icono = Icons.AutoMirrored.Outlined.ListAlt,
                    titulo = "Mis Casos",
                    contador = "$activos en proceso"
                )
            }
            when {
                state.cargandoTickets && state.tickets.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.tickets.isEmpty() -> item { MensajeVacio("Aún no tienes reclamos registrados.") }
                else -> items(state.tickets, key = { it.id.ifBlank { it.folio } }) { TarjetaTicket(it) }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Formulario
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
    onEnviar: (DatosReclamo) -> Unit,
    onCerrar: () -> Unit
) {
    var datos by remember(perfil) { mutableStateOf(contactoDesdePerfil(perfil)) }
    val incompleto = datos.productoNombre.isBlank() || datos.numeroSerie.isBlank() ||
        datos.facturaReferencia.isBlank() || datos.descripcionFalla.isBlank() ||
        datos.cliente.isBlank() || datos.contacto.isBlank() || datos.correo.isBlank()

    TarjetaTs(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Nuevo Reclamo",
                style = MaterialTheme.typography.titleLarge,
                color = TsSecundario,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCerrar) { Icon(Icons.Outlined.Close, "Cerrar", tint = TsNeutro) }
        }

        Text("EQUIPO", style = MaterialTheme.typography.labelMedium, color = TsNeutro)
        Spacer(Modifier.height(8.dp))
        Campo("Producto", datos.productoNombre, Icons.Outlined.Inventory2) { datos = datos.copy(productoNombre = it) }
        Campo("SKU (opcional)", datos.sku, Icons.Outlined.QrCode) { datos = datos.copy(sku = it) }
        Campo("Número de serie", datos.numeroSerie, Icons.AutoMirrored.Outlined.FactCheck) { datos = datos.copy(numeroSerie = it) }
        Campo("Factura / CCF de referencia", datos.facturaReferencia, Icons.AutoMirrored.Outlined.ReceiptLong) {
            datos = datos.copy(facturaReferencia = it)
        }
        EtiquetaCampo("Descripción de la falla")
        CampoTs(
            valor = datos.descripcionFalla,
            onCambio = { datos = datos.copy(descripcionFalla = it) },
            placeholder = "Comportamiento del equipo, códigos de error, pruebas realizadas…",
            singleLine = false,
            minLines = 3
        )

        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (datos.esDOA) TsExitoFondo else TsSuperficieSuave)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Falla de fábrica (DOA)", style = MaterialTheme.typography.titleSmall, color = TsSecundario)
                Text(
                    "El equipo falló en los primeros 7 días. El cambio se aprueba de inmediato.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TsNeutro
                )
            }
            Switch(checked = datos.esDOA, onCheckedChange = { datos = datos.copy(esDOA = it) })
        }

        Spacer(Modifier.height(16.dp))
        Text("CONTACTO", style = MaterialTheme.typography.labelMedium, color = TsNeutro)
        Spacer(Modifier.height(8.dp))
        Campo("Cliente / razón social", datos.cliente, Icons.Outlined.Business) { datos = datos.copy(cliente = it) }
        Campo("Persona de contacto", datos.contacto, Icons.Outlined.Person) { datos = datos.copy(contacto = it) }
        Campo("Teléfono", datos.telefono, Icons.Outlined.Phone, KeyboardType.Phone) { datos = datos.copy(telefono = it) }
        Campo("Correo de contacto", datos.correo, Icons.Outlined.Email, KeyboardType.Email) {
            datos = datos.copy(correo = it)
        }

        Spacer(Modifier.height(4.dp))
        Button(
            onClick = { onEnviar(datos) },
            enabled = !enviando && !incompleto,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (enviando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = TsSuperficie)
            else Text("Registrar reclamo", style = MaterialTheme.typography.titleSmall)
        }
        if (incompleto) {
            Text(
                "Completa los campos para enviar (el SKU es opcional).",
                style = MaterialTheme.typography.bodySmall,
                color = TsNeutro,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun Campo(
    etiqueta: String,
    valor: String,
    icono: ImageVector,
    teclado: KeyboardType = KeyboardType.Text,
    onCambio: (String) -> Unit
) {
    EtiquetaCampo(etiqueta)
    CampoTs(
        valor = valor,
        onCambio = onCambio,
        placeholder = etiqueta,
        leadingIcon = icono,
        keyboardOptions = KeyboardOptions(keyboardType = teclado)
    )
    Spacer(Modifier.height(12.dp))
}

// ---------------------------------------------------------------------------
// Casos registrados (incluye los creados en la web)
// ---------------------------------------------------------------------------

@Composable
private fun TarjetaTicket(t: TicketRMA) {
    TarjetaTs(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            IconoEnCaja(Icons.Outlined.Inventory2, tamano = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "#${t.folio}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FuenteMono,
                        fontWeight = FontWeight.Bold,
                        color = TsPrimario
                    )
                    Text(
                        "  ·  ${t.fecha}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TsNeutro
                    )
                }
                Text(
                    t.productoNombre.ifBlank { t.sku },
                    style = MaterialTheme.typography.titleSmall,
                    color = TsSecundario,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            EstadoChip(t.estado)
            if (t.esDOA) EtiquetaMono("Garantía DOA", fondo = TsExitoFondo, color = TsExitoTexto)
        }

        Spacer(Modifier.height(10.dp))
        CajaSuave {
            Text(
                t.descripcionFalla,
                style = MaterialTheme.typography.bodyMedium,
                color = TsSecundario,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                buildString {
                    append("Serie ${t.numeroSerie}")
                    if (t.facturaReferencia.isNotBlank()) append("  ·  Factura ${t.facturaReferencia}")
                },
                style = MaterialTheme.typography.labelSmall,
                color = TsNeutro
            )
        }

        if (t.diagnosticoTecnico.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Diagnóstico: ${t.diagnosticoTecnico}",
                style = MaterialTheme.typography.bodySmall,
                color = TsNeutro
            )
        }
        if (t.resolucion.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Resolución: ${t.resolucion}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TsSecundario
            )
        }
    }
}
