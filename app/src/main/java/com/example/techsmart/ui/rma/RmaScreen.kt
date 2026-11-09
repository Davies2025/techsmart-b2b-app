package com.example.techsmart.ui.rma

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VerifiedUser
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.TicketRMA
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.ui.components.*
import com.example.techsmart.ui.theme.*

private const val INDICE_CASOS = 2

@Composable
fun RmaScreen(viewModel: RmaViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val lista = rememberLazyListState()
    var formularioAbierto by remember { mutableStateOf(false) }
    var formularioVersion by remember { mutableIntStateOf(0) }
    var filtroEstado by remember { mutableStateOf("Todos") }

    val verdeEsmeralda = Color(0xFF0A876C)
    val azulReal = Color(0xFF155EEF)

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
    val casosDoa = state.tickets.count { it.esDOA }
    val cerrados = state.tickets.count { it.estado == "Cerrado" || it.estado == "Cambio Aprobado" }

    val ticketsFiltrados = remember(state.tickets, filtroEstado) {
        when (filtroEstado) {
            "En Proceso" -> state.tickets.filter { it.estado != "Cerrado" }
            "DOA 7 Días" -> state.tickets.filter { it.esDOA }
            "Resueltos" -> state.tickets.filter { it.estado == "Cerrado" || it.estado == "Cambio Aprobado" }
            else -> state.tickets
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
                            Color(0xFFE8F5F1),
                            TsFondo,
                            Color(0xFFEAF1FF)
                        )
                    )
                )
                .padding(padding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 28.dp.toPx()
                val dotRadius = 1.2.dp.toPx()
                val dotColor = verdeEsmeralda.copy(alpha = 0.06f)

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
                        colors = listOf(verdeEsmeralda.copy(alpha = 0.09f), Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.10f),
                        radius = size.width * 0.55f
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.10f),
                    radius = size.width * 0.55f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(azulReal.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.12f, size.height * 0.82f),
                        radius = size.width * 0.5f
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.82f),
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
                    TableroGarantiasRma(
                        activos = activos,
                        casosDoa = casosDoa,
                        resueltos = cerrados
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
                        TarjetaLanzadorReclamo(
                            onAbrirFormulario = { formularioAbierto = true }
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        EncabezadoCasosRma(activos = activos)

                        if (state.tickets.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val filtros = listOf(
                                    "Todos" to state.tickets.size,
                                    "En Proceso" to activos,
                                    "DOA 7 Días" to casosDoa,
                                    "Resueltos" to cerrados
                                )
                                items(filtros) { (nombre, conteo) ->
                                    ChipFiltroRma(
                                        texto = nombre,
                                        conteo = conteo,
                                        activo = filtroEstado == nombre,
                                        onClick = { filtroEstado = nombre }
                                    )
                                }
                            }
                        }
                    }
                }

                when {
                    state.cargandoTickets && state.tickets.isEmpty() -> item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = TsPrimario)
                        }
                    }
                    state.tickets.isEmpty() -> item {
                        EstadoSinReclamos(
                            onCrearReclamo = { formularioAbierto = true }
                        )
                    }
                    ticketsFiltrados.isEmpty() -> item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, TsBorde),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MensajeVacio("No hay casos en la categoría \"$filtroEstado\".")
                        }
                    }
                    else -> items(ticketsFiltrados, key = { it.id.ifBlank { it.folio } }) { ticket ->
                        TarjetaTicket(ticket)
                    }
                }
            }
        }
    }
}

@Composable
private fun TableroGarantiasRma(
    activos: Int,
    casosDoa: Int,
    resueltos: Int
) {
    val verdeProfundo = Color(0xFF064E3B)
    val verdeEsmeralda = Color(0xFF0A876C)
    val mentaPulso = Color(0xFF34D399)
    val azulNoche = Color(0xFF0A1931)

    Surface(
        shape = RoundedCornerShape(26.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(1.2.dp, mentaPulso.copy(alpha = 0.40f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(azulNoche, verdeProfundo, verdeEsmeralda)
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val cx = size.width * 0.90f
                val cy = size.height * 0.22f
                drawCircle(
                    color = mentaPulso.copy(alpha = 0.14f),
                    radius = size.width * 0.34f,
                    center = Offset(cx, cy)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
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
                            color = mentaPulso.copy(alpha = 0.20f),
                            border = BorderStroke(1.dp, mentaPulso.copy(alpha = 0.45f)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.VerifiedUser,
                                    contentDescription = null,
                                    tint = mentaPulso,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "CENTRO DE SOPORTE & RMA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = mentaPulso
                            )
                            Text(
                                text = "Garantía Techsmart Oficial",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Seguimiento técnico por número de serie. Si el equipo presenta falla en los primeros 7 días (DOA), el cambio directo se aprueba de inmediato.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f)
                )

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TarjetaMetricaRma(
                        modifier = Modifier.weight(1f),
                        numero = "$activos",
                        etiqueta = "En proceso",
                        colorNumero = Color.White
                    )
                    TarjetaMetricaRma(
                        modifier = Modifier.weight(1f),
                        numero = "$casosDoa",
                        etiqueta = "Casos DOA",
                        colorNumero = mentaPulso
                    )
                    TarjetaMetricaRma(
                        modifier = Modifier.weight(1f),
                        numero = "$resueltos",
                        etiqueta = "Resueltos",
                        colorNumero = Color(0xFF93C5FD)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.24f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("1. Reporte", "2. Bodega", "3. Diagnóstico", "4. Solución").forEach { paso ->
                            Text(
                                text = paso,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.88f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaMetricaRma(
    modifier: Modifier = Modifier,
    numero: String,
    etiqueta: String,
    colorNumero: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
        ) {
            Text(
                text = numero,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FuenteMono,
                fontWeight = FontWeight.ExtraBold,
                color = colorNumero
            )
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.80f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TarjetaLanzadorReclamo(onAbrirFormulario: () -> Unit) {
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        onClick = onAbrirFormulario,
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFF2F7FF), Color.White, Color(0xFFF0FBF7))
                    )
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TsPrimario,
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircleOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Nuevo Reclamo / Garantía",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsSecundario
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Registra número de serie, factura y diagnóstico inicial",
                        style = MaterialTheme.typography.bodySmall,
                        color = TsNeutro
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            color = verdeEsmeralda.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "DOA 7 Días",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = verdeEsmeralda,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            color = TsPrimario.copy(alpha = 0.10f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "SLA 48h",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TsPrimario,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            Surface(
                shape = CircleShape,
                color = TsPrimario,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EncabezadoCasosRma(activos: Int) {
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
                            listOf(Color(0xFF0A876C), TsPrimario)
                        )
                    )
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ListAlt,
                contentDescription = null,
                tint = TsPrimario,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Mis Casos RMA",
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
                text = "$activos EN PROCESO",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FuenteMono,
                fontWeight = FontWeight.ExtraBold,
                color = TsPrimario,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ChipFiltroRma(
    texto: String,
    conteo: Int,
    activo: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (activo) TsPrimario else Color.White,
        shadowElevation = if (activo) 3.dp else 1.dp,
        border = if (activo) null else BorderStroke(1.dp, TsPrimario.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = texto,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (activo) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (activo) Color.White else TsSecundario
            )
            Spacer(Modifier.width(6.dp))
            Surface(
                color = if (activo) Color.White.copy(alpha = 0.22f) else TsAzulSuave,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = "$conteo",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FuenteMono,
                    fontWeight = FontWeight.Bold,
                    color = if (activo) Color.White else TsPrimario,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun EstadoSinReclamos(onCrearReclamo: () -> Unit) {
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, verdeEsmeralda.copy(alpha = 0.22f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF0FBF7), Color.White)
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = verdeEsmeralda.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, verdeEsmeralda.copy(alpha = 0.25f)),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = verdeEsmeralda,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Sin incidencias activas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TsSecundario
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Aún no tienes reclamos registrados. Tus equipos cuentan con respaldo oficial Techsmart.",
                style = MaterialTheme.typography.bodySmall,
                color = TsNeutro,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = onCrearReclamo,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, TsPrimario)
            ) {
                Icon(Icons.Outlined.AddCircleOutline, null, Modifier.size(16.dp), tint = TsPrimario)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Crear un reporte ahora",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = TsPrimario
                )
            }
        }
    }
}

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
    var pasoActual by remember { mutableIntStateOf(1) }

    val paso1Completo = datos.productoNombre.isNotBlank() &&
            datos.numeroSerie.isNotBlank() &&
            datos.facturaReferencia.isNotBlank() &&
            datos.descripcionFalla.isNotBlank()

    val incompleto = !paso1Completo ||
            datos.cliente.isBlank() ||
            datos.contacto.isBlank() ||
            datos.correo.isBlank()

    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(verdeEsmeralda, TsPrimario, Color(0xFF00C2FF))
                        )
                    )
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "REGISTRO DE GARANTÍA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = TsPrimario
                        )
                        Text(
                            text = "Nuevo Reclamo RMA",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsSecundario
                        )
                    }

                    Surface(
                        onClick = onCerrar,
                        shape = CircleShape,
                        color = TsSuperficieSuave,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Cerrar",
                                tint = TsSecundario,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonPasoRma(
                        modifier = Modifier.weight(1f),
                        numero = "1",
                        titulo = "Equipo y Falla",
                        activo = pasoActual == 1,
                        completado = paso1Completo,
                        onClick = { pasoActual = 1 }
                    )
                    BotonPasoRma(
                        modifier = Modifier.weight(1f),
                        numero = "2",
                        titulo = "Titular y Envío",
                        activo = pasoActual == 2,
                        completado = !incompleto,
                        onClick = { pasoActual = 2 }
                    )
                }

                Spacer(Modifier.height(16.dp))

                if (pasoActual == 1) {
                    Campo("Producto o modelo", datos.productoNombre, Icons.Outlined.Inventory2) {
                        datos = datos.copy(productoNombre = it)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Campo("SKU (opcional)", datos.sku, Icons.Outlined.QrCode) {
                                datos = datos.copy(sku = it)
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Campo("Factura / CCF", datos.facturaReferencia, Icons.AutoMirrored.Outlined.ReceiptLong) {
                                datos = datos.copy(facturaReferencia = it)
                            }
                        }
                    }
                    Campo("Número de serie (S/N)", datos.numeroSerie, Icons.AutoMirrored.Outlined.FactCheck) {
                        datos = datos.copy(numeroSerie = it)
                    }

                    EtiquetaCampo("Descripción detallada de la falla")
                    CampoTs(
                        valor = datos.descripcionFalla,
                        onCambio = { datos = datos.copy(descripcionFalla = it) },
                        placeholder = "Comportamiento del equipo, códigos de error, pruebas realizadas…",
                        singleLine = false,
                        minLines = 3
                    )

                    Spacer(Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (datos.esDOA) Color(0xFFDDF7EF) else Color(0xFFF6F9FF),
                        border = BorderStroke(
                            1.dp,
                            if (datos.esDOA) verdeEsmeralda else TsPrimario.copy(alpha = 0.18f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datos = datos.copy(esDOA = !datos.esDOA) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (datos.esDOA) verdeEsmeralda else TsPrimario.copy(alpha = 0.12f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = if (datos.esDOA) Color.White else TsPrimario,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "Falla de fábrica (DOA - 7 días)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (datos.esDOA) verdeEsmeralda else TsSecundario
                                )
                                Text(
                                    text = "Si falló en los primeros 7 días, el cambio se aprueba de inmediato.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TsNeutro
                                )
                            }
                            Switch(
                                checked = datos.esDOA,
                                onCheckedChange = { datos = datos.copy(esDOA = it) }
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { pasoActual = 2 },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "Continuar a Datos de Contacto",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                    }
                } else {
                    Campo("Cliente / razón social", datos.cliente, Icons.Outlined.Business) {
                        datos = datos.copy(cliente = it)
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

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { pasoActual = 1 },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, TsBorde),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Equipo")
                        }

                        Button(
                            onClick = { onEnviar(datos) },
                            enabled = !enviando && !incompleto,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = verdeEsmeralda,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp)
                        ) {
                            if (enviando) {
                                CircularProgressIndicator(
                                    Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Registrar reclamo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (incompleto) {
                        Text(
                            text = "Completa los datos del equipo (Paso 1) y de contacto para enviar (el SKU es opcional).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonPasoRma(
    modifier: Modifier = Modifier,
    numero: String,
    titulo: String,
    activo: Boolean,
    completado: Boolean,
    onClick: () -> Unit
) {
    val verdeEsmeralda = Color(0xFF0A876C)
    val colorBorde = when {
        activo -> TsPrimario
        completado -> verdeEsmeralda.copy(alpha = 0.45f)
        else -> TsBorde
    }
    val colorFondo = when {
        activo -> TsPrimario.copy(alpha = 0.10f)
        completado -> Color(0xFFF0FBF7)
        else -> TsSuperficieSuave
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = colorFondo,
        border = BorderStroke(1.2.dp, colorBorde),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            completado -> verdeEsmeralda
                            activo -> TsPrimario
                            else -> TsNeutro.copy(alpha = 0.25f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numero,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FuenteMono,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (activo) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (activo) TsPrimario else TsSecundario,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    Column(modifier = Modifier.fillMaxWidth()) {
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
}

@Composable
private fun TarjetaTicket(t: TicketRMA) {
    var expandido by remember { mutableStateOf(false) }
    val (_, colorEstado) = coloresEstado(t.estado)
    val etapaActual = etapaDeEstado(t.estado)
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, colorEstado.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clip(RoundedCornerShape(22.dp))
            .clickable { expandido = !expandido }
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(colorEstado)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = colorEstado.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "#${t.folio}",
                                style = MaterialTheme.typography.titleSmall,
                                fontFamily = FuenteMono,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorEstado,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (t.esDOA) {
                            EtiquetaMono(
                                "Garantía DOA",
                                fondo = TsExitoFondo,
                                color = TsExitoTexto
                            )
                        }
                    }

                    EstadoChip(t.estado)
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoEnCaja(
                        icono = Icons.Outlined.Inventory2,
                        tamano = 42.dp,
                        fondo = colorEstado.copy(alpha = 0.10f),
                        tinte = colorEstado
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = t.productoNombre.ifBlank { t.sku },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsSecundario,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = TsNeutro,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "Registrado: ${t.fecha}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TsNeutro
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                BarraProgresoRma(etapaActual = etapaActual, colorActivo = colorEstado)

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PildoraTecnica(
                        modifier = Modifier.weight(1f),
                        titulo = "N° SERIE (S/N)",
                        valor = t.numeroSerie.ifBlank { "N/A" }
                    )
                    if (t.facturaReferencia.isNotBlank()) {
                        PildoraTecnica(
                            modifier = Modifier.weight(1f),
                            titulo = "FACTURA / REF",
                            valor = t.facturaReferencia
                        )
                    } else if (t.sku.isNotBlank()) {
                        PildoraTecnica(
                            modifier = Modifier.weight(1f),
                            titulo = "CÓDIGO SKU",
                            valor = t.sku
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF6F9FF),
                    border = BorderStroke(1.dp, TsBorde),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "FALLA REPORTADA",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsNeutro
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = t.descripcionFalla,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TsSecundario,
                            maxLines = if (expandido) 10 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expandido || t.diagnosticoTecnico.isNotBlank() || t.resolucion.isNotBlank()
                ) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        if (t.diagnosticoTecnico.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = TsAvisoFondo.copy(alpha = 0.55f),
                                border = BorderStroke(1.dp, TsAvisoTexto.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Build,
                                        contentDescription = null,
                                        tint = TsAvisoTexto,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Diagnóstico Técnico",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TsAvisoTexto
                                        )
                                        Text(
                                            text = t.diagnosticoTecnico,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TsSecundario
                                        )
                                    }
                                }
                            }
                        }

                        if (t.resolucion.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFDDF7EF),
                                border = BorderStroke(1.dp, verdeEsmeralda.copy(alpha = 0.30f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = verdeEsmeralda,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Resolución Oficial",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = verdeEsmeralda
                                        )
                                        Text(
                                            text = t.resolucion,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TsSecundario
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Etapa $etapaActual de 4",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FuenteMono,
                        fontWeight = FontWeight.Bold,
                        color = TsNeutro
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (expandido) "Menos detalles" else "Ficha completa",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TsPrimario
                        )
                        Icon(
                            imageVector = if (expandido) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
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

@Composable
private fun PildoraTecnica(
    modifier: Modifier = Modifier,
    titulo: String,
    valor: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = TsSuperficieSuave,
        border = BorderStroke(1.dp, TsBorde)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TsNeutro
            )
            Spacer(Modifier.height(1.dp))
            Text(
                text = valor,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FuenteMono,
                fontWeight = FontWeight.ExtraBold,
                color = TsSecundario,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BarraProgresoRma(etapaActual: Int, colorActivo: Color) {
    val etiquetas = listOf("Reporte", "Bodega", "Diagnóstico", "Solución")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 1..4) {
                val activo = i <= etapaActual
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (activo) colorActivo else TsBorde
                        )
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            etiquetas.forEachIndexed { index, nombre ->
                val paso = index + 1
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = if (paso == etapaActual) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (paso <= etapaActual) colorActivo else TsNeutro
                )
            }
        }
    }
}

private fun etapaDeEstado(estado: String): Int = when (estado) {
    "Reportado", "En Revisión" -> 1
    "Recibido en Bodega", "En Bodega" -> 2
    "En Diagnóstico Técnico" -> 3
    "Cambio Aprobado", "Cerrado", "Entregada", "Aprobada" -> 4
    else -> 1
}