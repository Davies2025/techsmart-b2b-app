package com.example.techsmart.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.ItemCotizacion
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.ui.components.*
import com.example.techsmart.ui.theme.*

// Posición del encabezado "Cotizaciones Activas" dentro de la lista (banner + solicitud)
private const val INDICE_ACTIVAS = 2

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onIrACatalogo: () -> Unit,
    onComprarAhora: () -> Unit,
    onVerPedido: (folio: String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val lista = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
        viewModel.observarMisCotizaciones()
    }

    LaunchedEffect(state.folioGuardado) {
        state.folioGuardado?.let { folio ->
            viewModel.consumirCotizacionGuardada()
            lista.animateScrollToItem(INDICE_ACTIVAS)
            snackbar.showSnackbar("Cotización $folio enviada. Quedó en revisión.")
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            viewModel.consumirError()
            snackbar.showSnackbar(it)
        }
    }

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
                    icono = Icons.Filled.Bolt,
                    titulo = "Cotizador B2B & Express",
                    descripcion = "Precios por volumen e IVA desglosado. Arma tu lote desde el catálogo " +
                        "y envíalo para revisión."
                )
            }
            item {
                if (state.items.isEmpty()) LoteVacio(onIrACatalogo)
                else NuevaSolicitud(state, viewModel, onIrACatalogo, onComprarAhora)
            }
            item {
                EncabezadoSeccion(
                    icono = Icons.Outlined.History,
                    titulo = "Cotizaciones Activas",
                    contador = state.misCotizaciones.size.let { if (it == 1) "1 registrada" else "$it registradas" }
                )
            }
            when {
                state.cargandoCotizaciones && state.misCotizaciones.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.misCotizaciones.isEmpty() -> item {
                    MensajeVacio("Aún no tienes cotizaciones.")
                }
                else -> items(state.misCotizaciones, key = { it.id.ifBlank { it.folio } }) { cot ->
                    TarjetaCotizacion(cot, onVerPedido)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Nueva solicitud
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

@Composable
private fun LoteVacio(onIrACatalogo: () -> Unit) {
    TarjetaTs(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Outlined.NoteAdd, null, tint = TsPrimario)
            Spacer(Modifier.width(8.dp))
            Text("Nueva Solicitud", style = MaterialTheme.typography.titleLarge, color = TsSecundario)
        }
        Spacer(Modifier.height(12.dp))
        CajaSuave {
            Text("Tu lote está vacío", style = MaterialTheme.typography.titleSmall, color = TsSecundario)
            Text(
                "Agrega productos desde el catálogo para armar la cotización.",
                style = MaterialTheme.typography.bodySmall,
                color = TsNeutro
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onIrACatalogo,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Outlined.Inventory2, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ir al catálogo")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NuevaSolicitud(
    state: CartUiState,
    viewModel: CartViewModel,
    onIrACatalogo: () -> Unit,
    onComprarAhora: () -> Unit
) {
    // Se precarga con el perfil; luego el cliente puede editarlo
    var datos by remember(state.perfil) { mutableStateOf(datosDesdePerfil(state.perfil)) }
    val esEmpresa = datos.tipoCliente == Cotizacion.TIPO_EMPRESA
    val faltanDatos = datos.razonSocial.isBlank() || datos.contacto.isBlank() ||
        datos.correo.isBlank() || datos.telefono.isBlank() || (esEmpresa && datos.nit.isBlank())

    TarjetaTs(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Outlined.NoteAdd, null, tint = TsPrimario)
            Spacer(Modifier.width(8.dp))
            Text(
                "Nueva Solicitud",
                style = MaterialTheme.typography.titleLarge,
                color = TsSecundario,
                modifier = Modifier.weight(1f)
            )
            EtiquetaMono("${state.items.sumOf { it.cantidad }} unid.")
        }
        Spacer(Modifier.height(12.dp))

        // ---------- Tipo de cliente ----------
        SelectorSegmentado(
            opciones = listOf(Cotizacion.TIPO_EMPRESA, Cotizacion.TIPO_PARTICULAR),
            seleccion = datos.tipoCliente,
            onSeleccion = { datos = datos.copy(tipoCliente = it) }
        )

        // ---------- Partidas ----------
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "PARTIDAS DEL LOTE",
                style = MaterialTheme.typography.labelMedium,
                color = TsNeutro,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onIrACatalogo, contentPadding = PaddingValues(horizontal = 8.dp)) {
                Text("+ Agregar más", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.items.forEach { item ->
                PartidaEditable(
                    item = item,
                    onCantidad = { viewModel.cambiarCantidad(item.productoId, it) },
                    onEliminar = { viewModel.eliminarItem(item.productoId) }
                )
            }
        }

        // ---------- Totales ----------
        Spacer(Modifier.height(12.dp))
        CajaSuave {
            FilaMonto("Subtotal neto", dinero(state.totales.subtotalNeto))
            if (state.totales.descuento > 0.0) {
                FilaMonto(
                    "Ahorro por volumen",
                    "−" + dinero(state.totales.descuento),
                    colorValor = TsExitoTexto
                )
            }
            FilaMonto("IVA (13%)", dinero(state.totales.iva))
            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = TsBorde)
            FilaMonto("Total", dinero(state.totales.total), destacado = true)
        }

        // ---------- Dos salidas del lote: comprar o cotizar ----------
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onComprarAhora,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TsSecundario),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Outlined.ShoppingCartCheckout, null, Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Comprar ahora", style = MaterialTheme.typography.titleSmall)
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f), color = TsBorde)
            Text(
                "  o envía una cotización para revisión  ",
                style = MaterialTheme.typography.labelSmall,
                color = TsNeutro
            )
            HorizontalDivider(Modifier.weight(1f), color = TsBorde)
        }

        // ---------- Datos del cliente ----------
        Campo(if (esEmpresa) "Razón social" else "Nombre completo", datos.razonSocial, Icons.Outlined.Business) {
            datos = datos.copy(razonSocial = it)
        }
        Campo(if (esEmpresa) "NIT / NRC" else "DUI (opcional)", datos.nit, Icons.Outlined.Badge) {
            datos = datos.copy(nit = it)
        }
        Campo("Persona de contacto", datos.contacto, Icons.Outlined.Person) {
            datos = datos.copy(contacto = it)
        }
        Campo("Teléfono / WhatsApp", datos.telefono, Icons.Outlined.Phone, KeyboardType.Phone) {
            datos = datos.copy(telefono = it)
        }
        Campo("Correo de contacto", datos.correo, Icons.Outlined.Email, KeyboardType.Email) {
            datos = datos.copy(correo = it)
        }

        // ---------- Entrega y validez ----------
        EtiquetaCampo("Tiempo de entrega")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Cotizacion.TIEMPOS_ENTREGA.forEach { t ->
                OpcionChip(t, datos.tiempoEntrega == t) { datos = datos.copy(tiempoEntrega = t) }
            }
        }
        Spacer(Modifier.height(12.dp))
        EtiquetaCampo("Validez de la oferta")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7, 15, 30).forEach { d ->
                OpcionChip("$d días", datos.validezDias == d) { datos = datos.copy(validezDias = d) }
            }
        }
        Spacer(Modifier.height(12.dp))
        EtiquetaCampo("Notas (opcional)")
        CampoTs(
            valor = datos.notas,
            onCambio = { datos = datos.copy(notas = it) },
            placeholder = "Ej. facturación separada, entrega en dos direcciones…",
            singleLine = false,
            minLines = 3
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.guardarCotizacion(datos) },
            enabled = !state.isSubmitting && !faltanDatos,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = TsSuperficie)
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Generar y Enviar Cotización", style = MaterialTheme.typography.titleSmall)
            }
        }
        if (faltanDatos) {
            Text(
                "Completa los datos del cliente para enviar.",
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
    icono: androidx.compose.ui.graphics.vector.ImageVector,
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

@Composable
private fun OpcionChip(texto: String, activo: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (activo) TsAzulSuave else TsSuperficieSuave,
        border = if (activo) androidx.compose.foundation.BorderStroke(1.5.dp, TsPrimario) else null
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
            color = if (activo) TsPrimario else TsNeutro,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PartidaEditable(
    item: ItemCotizacion,
    onCantidad: (Int) -> Unit,
    onEliminar: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TsSuperficieSuave)
            .padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f).padding(top = 4.dp)) {
                Text(
                    item.nombreMostrado,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TsSecundario,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(item.skuMostrado, style = MaterialTheme.typography.labelSmall, color = TsNeutro)
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Quitar", tint = TsNeutro)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onCantidad(item.cantidad - 1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Menos", tint = TsSecundario)
            }
            Text("${item.cantidad}", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = { onCantidad(item.cantidad + 1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Más", tint = TsSecundario)
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    dinero(item.subtotalMostrado),
                    style = MaterialTheme.typography.titleSmall,
                    color = TsSecundario
                )
                val conDescuento = item.precioBase > item.precioUnitario
                Text(
                    "${dinero(item.precioUnitario)} c/u" + if (conDescuento) " · volumen" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (conDescuento) TsExitoTexto else TsNeutro
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Cotizaciones registradas (incluye las creadas en la web)
// ---------------------------------------------------------------------------

@Composable
private fun TarjetaCotizacion(cot: Cotizacion, onVerPedido: (String) -> Unit) {
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
            "Cliente: ${cot.razonSocial}",
            style = MaterialTheme.typography.bodySmall,
            color = TsNeutro,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (cot.esCompra) {
            Spacer(Modifier.height(4.dp))
            EtiquetaMono("Compra · ${cot.pagoReferencia}", fondo = TsSuperficieSuave, color = TsSecundario)
        }
        Spacer(Modifier.height(10.dp))

        CajaSuave {
            if (abierta) {
                cot.items.forEach { item ->
                    FilaMonto("${item.cantidad} × ${item.nombreMostrado}", dinero(item.subtotalMostrado))
                }
                HorizontalDivider(Modifier.padding(vertical = 6.dp), color = TsBorde)
                FilaMonto("Subtotal neto", dinero(cot.subtotalNeto))
                if (cot.descuento > 0.0) {
                    FilaMonto("Ahorro por volumen", "−" + dinero(cot.descuento), colorValor = TsExitoTexto)
                }
                FilaMonto("IVA (13%)", dinero(cot.iva))
                if (cot.tiempoEntrega.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Entrega: ${cot.tiempoEntrega}", style = MaterialTheme.typography.bodySmall, color = TsNeutro)
                }
                if (cot.notas.isNotBlank()) {
                    Text("Notas: ${cot.notas}", style = MaterialTheme.typography.bodySmall, color = TsNeutro)
                }
            } else {
                Text(
                    cot.items.joinToString(" + ") { "${it.cantidad}x ${it.nombreMostrado}" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TsSecundario,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                Text("Total", style = MaterialTheme.typography.labelSmall, color = TsNeutro)
                Text(
                    dinero(cot.total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = TsSecundario
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Schedule, null, tint = TsNeutro, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                buildString {
                    append(cot.fecha)
                    // "15 nov 2026 (en 8 días)" -> "15 nov 2026"; la web puede guardar "Vigencia 8 días"
                    val vence = cot.vencimiento.substringBefore(" (")
                    if (vence.isNotBlank() && vence != "Pagada") append("  ·  Vence: $vence")
                },
                style = MaterialTheme.typography.labelSmall,
                color = TsNeutro,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (cot.esCompra) {
                TextButton(onClick = { onVerPedido(cot.folio) }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                    Text("Seguimiento", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Text(
                    if (abierta) "Ocultar" else "Ver detalle",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TsPrimario
                )
            }
        }
    }
}
