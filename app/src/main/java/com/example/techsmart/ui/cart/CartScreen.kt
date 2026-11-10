package com.example.techsmart.ui.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.ShoppingCartCheckout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.ItemCotizacion
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.ui.components.*
import com.example.techsmart.ui.theme.*

private const val INDICE_ACTIVAS = 1

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

    val azulReal = Color(0xFF155EEF)
    val verdeEsmeralda = Color(0xFF0A876C)

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
        viewModel.observarMisCotizaciones()
    }

    LaunchedEffect(state.folioGuardado) {
        state.folioGuardado?.let { folio ->
            viewModel.consumirCotizacionGuardada()
            lista.animateScrollToItem(INDICE_ACTIVAS)
            snackbar.showSnackbar("Cotización $folio enviada.")
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            viewModel.consumirError()
            snackbar.showSnackbar(it)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) }
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
                state = lista,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HeaderCotizador(
                        unidadesEnLote = state.items.sumOf { it.cantidad },
                        cotizacionesTotales = state.misCotizaciones.size
                    )
                }
                if (state.items.isNotEmpty()) {
                    item {
                        NuevaSolicitud(state, viewModel, onIrACatalogo, onComprarAhora)
                    }
                }
                item {
                    EncabezadoHistorial(
                        total = state.misCotizaciones.size
                    )
                }
                when {
                    state.cargandoCotizaciones && state.misCotizaciones.isEmpty() -> item {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TsPrimario)
                        }
                    }
                    state.misCotizaciones.isEmpty() -> item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, TsBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MensajeVacio("Aún no tienes cotizaciones registradas.")
                        }
                    }
                    else -> items(state.misCotizaciones, key = { it.id.ifBlank { it.folio } }) { cot ->
                        TarjetaCotizacion(cot, onVerPedido)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderCotizador(
    unidadesEnLote: Int,
    cotizacionesTotales: Int
) {
    val azulNoche = Color(0xFF0A1931)
    val azulReal = Color(0xFF155EEF)
    val cianPulso = Color(0xFF00D2FF)

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
                val cy = size.height * 0.25f
                drawCircle(
                    color = cianPulso.copy(alpha = 0.14f),
                    radius = size.width * 0.32f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = size.width * 0.22f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = cianPulso.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.4f)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Bolt,
                                    contentDescription = null,
                                    tint = cianPulso,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Techsmart",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = cianPulso
                            )
                            Text(
                                text = "Cotizador B2B & Express",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Descuentos automáticos, cálculo de IVA 13% y compra inmediata.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = if (unidadesEnLote > 0) "• $unidadesEnLote unid. en lote" else "• Lote disponible",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "• $cotizacionesTotales en historial",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoHistorial(total: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            listOf(TsPrimario, Color(0xFF00C2FF))
                        )
                    )
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                tint = TsPrimario,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Cotizaciones del Usuario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TsSecundario
            )
        }

        Surface(
            color = TsPrimario.copy(alpha = 0.12f),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.22f))
        ) {
            Text(
                text = if (total == 1) "1 REGISTRADA" else "$total REGISTRADAS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = TsPrimario,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

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
private fun NuevaSolicitud(
    state: CartUiState,
    viewModel: CartViewModel,
    onIrACatalogo: () -> Unit,
    onComprarAhora: () -> Unit
) {
    var datos by remember(state.perfil) { mutableStateOf(datosDesdePerfil(state.perfil)) }
    var mostrarFormularioCotizacion by remember { mutableStateOf(false) }

    val esEmpresa = datos.tipoCliente == Cotizacion.TIPO_EMPRESA
    val faltanDatos = datos.razonSocial.isBlank() || datos.contacto.isBlank() ||
            datos.correo.isBlank() || datos.telefono.isBlank() || (esEmpresa && datos.nit.isBlank())

    val verdeEsmeralda = Color(0xFF0A876C)
    val azulNoche = Color(0xFF0A1931)
    val cianPulso = Color(0xFF00D2FF)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 5.dp,
        border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(TsPrimario, cianPulso, verdeEsmeralda)
                        )
                    )
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconoEnCaja(
                            icono = Icons.AutoMirrored.Outlined.NoteAdd,
                            tamano = 42.dp,
                            fondo = TsPrimario.copy(alpha = 0.10f),
                            tinte = TsPrimario
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MI LOTE ACTIVO",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = TsSecundario
                            )
                            Text(
                                text = "${state.items.size} productos · ${state.items.sumOf { it.cantidad }} unidades",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TsNeutro
                            )
                        }
                    }

                    Surface(
                        onClick = onIrACatalogo,
                        shape = RoundedCornerShape(50),
                        color = TsPrimario.copy(alpha = 0.10f),
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

                Spacer(Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.items.forEach { item ->
                        PartidaEditable(
                            item = item,
                            onCantidad = { viewModel.cambiarCantidad(item.productoId, it) },
                            onEliminar = { viewModel.eliminarItem(item.productoId) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = azulNoche,
                    border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal neto", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                            Text(dinero(state.totales.subtotalNeto), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        if (state.totales.descuento > 0.0) {
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Ahorro por volumen", style = MaterialTheme.typography.bodySmall, color = Color(0xFF34D399))
                                Text("−" + dinero(state.totales.descuento), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("IVA (13%)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                            Text(dinero(state.totales.iva), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color.White.copy(alpha = 0.15f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total del Lote",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = dinero(state.totales.total),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = cianPulso
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onComprarAhora,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = verdeEsmeralda,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(Icons.Outlined.ShoppingCartCheckout, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Comprar ahora",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(10.dp))

                Surface(
                    onClick = { mostrarFormularioCotizacion = !mostrarFormularioCotizacion },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF3F7FF),
                    border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.28f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = TsPrimario,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Solicitar Cotización Formal",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TsPrimario
                                )
                                Text(
                                    text = "Envía el lote a revisión comercial",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TsNeutro
                                )
                            }
                        }
                        Icon(
                            imageVector = if (mostrarFormularioCotizacion) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TsPrimario
                        )
                    }
                }

                AnimatedVisibility(visible = mostrarFormularioCotizacion) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        HorizontalDivider(color = TsBorde)
                        Spacer(Modifier.height(14.dp))

                        EtiquetaCampo("Perfil de facturación")
                        SelectorSegmentado(
                            opciones = listOf(Cotizacion.TIPO_EMPRESA, Cotizacion.TIPO_PARTICULAR),
                            seleccion = datos.tipoCliente,
                            onSeleccion = { datos = datos.copy(tipoCliente = it) }
                        )

                        Spacer(Modifier.height(14.dp))

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

                        EtiquetaCampo("Tiempo de entrega")
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = TsSuperficie)
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Generar y Enviar Cotización",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (faltanDatos) {
                            Text(
                                text = "Completa los datos del cliente para enviar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }
                }
            }
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

@Composable
private fun OpcionChip(texto: String, activo: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (activo) TsPrimario else TsSuperficieSuave,
        border = BorderStroke(1.dp, if (activo) TsPrimario else TsBorde),
        shadowElevation = if (activo) 2.dp else 0.dp
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
            color = if (activo) Color.White else TsSecundario,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun PartidaEditable(
    item: ItemCotizacion,
    onCantidad: (Int) -> Unit,
    onEliminar: () -> Unit
) {
    val conDescuento = item.precioBase > item.precioUnitario
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (conDescuento) Color(0xFFF2FBF8) else Color(0xFFF6F9FF),
        border = BorderStroke(
            1.dp,
            if (conDescuento) verdeEsmeralda.copy(alpha = 0.28f) else TsPrimario.copy(alpha = 0.14f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp)
                ) {
                    Text(
                        text = item.nombreMostrado,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = TsSecundario,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "SKU: ${item.skuMostrado}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TsNeutro
                    )
                }
                IconButton(
                    onClick = onEliminar,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Quitar",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
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
                            fontWeight = FontWeight.ExtraBold,
                            color = TsSecundario,
                            modifier = Modifier.padding(horizontal = 6.dp)
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

                Spacer(Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Text(
                        text = dinero(item.subtotalMostrado),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (conDescuento) verdeEsmeralda else TsPrimario
                    )
                    Text(
                        text = "${dinero(item.precioUnitario)} c/u" + if (conDescuento) " · Oferta vol." else "",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (conDescuento) FontWeight.Bold else FontWeight.Normal,
                        color = if (conDescuento) verdeEsmeralda else TsNeutro
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaCotizacion(cot: Cotizacion, onVerPedido: (String) -> Unit) {
    var abierta by remember { mutableStateOf(false) }
    val (_, colorTextoEstado) = coloresEstado(cot.estado)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, colorTextoEstado.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(22.dp))
            .clickable { abierta = !abierta }
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(colorTextoEstado)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = colorTextoEstado.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#${cot.folio}",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FuenteMono,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorTextoEstado,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (cot.esCompra) {
                            EtiquetaMono(
                                "Compra",
                                fondo = Color(0xFFDDF7EF),
                                color = Color(0xFF0A876C)
                            )
                        }
                    }

                    EstadoChip(cot.estado)
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Cliente: ${cot.razonSocial}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TsSecundario,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (cot.esCompra && cot.pagoReferencia.isNotBlank()) {
                    Text(
                        text = "Ref: ${cot.pagoReferencia}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TsNeutro
                    )
                }

                Spacer(Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF6F9FF),
                    border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        if (abierta) {
                            cot.items.forEach { item ->
                                FilaMonto("${item.cantidad} × ${item.nombreMostrado}", dinero(item.subtotalMostrado))
                            }
                            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = TsBorde)
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
                                text = cot.items.joinToString(" + ") { "${it.cantidad}x ${it.nombreMostrado}" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = TsSecundario,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${cot.items.sumOf { it.cantidad }} unid. totales",
                                style = MaterialTheme.typography.labelSmall,
                                color = TsNeutro
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total IVA incl.", style = MaterialTheme.typography.labelSmall, color = TsNeutro)
                                Text(
                                    text = dinero(cot.total),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TsPrimario
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Schedule, null, tint = TsNeutro, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = buildString {
                            append(cot.fecha)
                            val vence = cot.vencimiento.substringBefore(" (")
                            if (vence.isNotBlank() && vence != "Pagada") append(" · Vence: $vence")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = TsNeutro,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (cot.esCompra) {
                        Surface(
                            onClick = { onVerPedido(cot.folio) },
                            shape = RoundedCornerShape(50),
                            color = TsPrimario
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Seguimiento",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = TsPrimario.copy(alpha = 0.10f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (abierta) "Ocultar" else "Ver detalle",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TsPrimario
                                )
                                Icon(
                                    imageVector = if (abierta) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
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
}