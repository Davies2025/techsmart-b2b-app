package com.example.techsmart.ui.catalog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val azulReal = Color(0xFF155EEF)
    val verdeEsmeralda = Color(0xFF0A876C)

    productoSeleccionado?.let { prod ->
        ModalDetalleProducto(
            producto = prod,
            onDismiss = { productoSeleccionado = null },
            onAgregar = { cantidad ->
                onAgregarAlCarrito(prod, cantidad)
                productoSeleccionado = null
                scope.launch {
                    snackbar.currentSnackbarData?.dismiss()
                    snackbar.showSnackbar("$cantidad × ${prod.nombre} agregado al carrito")
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (unidadesEnLote > 0) BarraLote(unidadesEnLote, subtotalLote, onVerLote)
        }
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
                        center = Offset(size.width * 0.9f, size.height * 0.08f),
                        radius = size.width * 0.55f
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.08f),
                    radius = size.width * 0.55f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(verdeEsmeralda.copy(alpha = 0.07f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.85f),
                        radius = size.width * 0.5f
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.85f),
                    radius = size.width * 0.5f
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp)
                ) {
                    TextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        placeholder = {
                            Text(
                                "Buscar por equipo, marca o SKU...",
                                color = TsNeutro.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = TsPrimario.copy(alpha = 0.10f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = TsPrimario,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        trailingIcon = if (busqueda.isNotEmpty()) {
                            {
                                IconButton(onClick = { busqueda = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Limpiar búsqueda",
                                        tint = TsNeutro
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                when (val s = state) {
                    is CatalogUiState.Loading -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = TsPrimario) }

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

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                ChipCategoria("Todos", s.productos.size, categoria == null) { categoria = null }
                            }
                            items(categorias) { c ->
                                val conteoCat = remember(s.productos, c) {
                                    s.productos.count { it.categoria == c }
                                }
                                ChipCategoria(c, conteoCat, categoria == c) {
                                    categoria = if (categoria == c) null else c
                                }
                            }
                        }

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
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
                                Text(
                                    text = categoria ?: "Catálogo",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TsSecundario,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                color = TsPrimario.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.20f))
                            ) {
                                Text(
                                    text = "${filtrados.size} disponibles",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TsPrimario,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        if (filtrados.isEmpty()) {
                            MensajeVacio(
                                if (s.productos.isEmpty()) "No hay productos disponibles."
                                else "Ningún producto coincide con la búsqueda."
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 6.dp,
                                    bottom = 28.dp
                                )
                            ) {
                                items(filtrados, key = { it.id }) { producto ->
                                    TarjetaProductoCompacta(
                                        producto = producto,
                                        onVerDetalle = { productoSeleccionado = producto },
                                        onAgregarRapido = {
                                            onAgregarAlCarrito(producto, 1)
                                            scope.launch {
                                                snackbar.currentSnackbarData?.dismiss()
                                                snackbar.showSnackbar("1 × ${producto.nombre} agregado al carrito")
                                            }
                                        }
                                    )
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
        color = if (activo) TsPrimario else Color.White,
        shadowElevation = if (activo) 4.dp else 1.dp,
        border = if (activo) null else BorderStroke(1.dp, TsPrimario.copy(alpha = 0.14f))
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                texto,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (activo) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (activo) Color.White else TsSecundario
            )
            if (conteo != null) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    color = if (activo) Color.White.copy(alpha = 0.22f) else TsAzulSuave,
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "$conteo",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (activo) Color.White else TsPrimario,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaProductoCompacta(
    producto: Producto,
    onVerDetalle: () -> Unit,
    onAgregarRapido: () -> Unit
) {
    val (colorAcento, fondoClaro, fondoMedio) = coloresPorCategoria(producto.categoria)
    val (textoStock, fondoStock, colorStock) = estadoStock(producto)
    val mejorOferta = producto.precioVolumen
        .filter { it.precioEfectivo > 0.0 && it.minQty > 1 && it.precioEfectivo < producto.precio }
        .minByOrNull { it.precioEfectivo }

    val habilitado = producto.stock > 0 || producto.disponibilidad == "bajo_pedido"

    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.2.dp, colorAcento.copy(alpha = 0.28f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onVerDetalle)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(fondoClaro, Color.White, fondoMedio)
                    )
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = colorAcento.copy(alpha = 0.09f),
                    radius = size.width * 0.25f,
                    center = Offset(size.width * 0.92f, size.height * 0.18f)
                )
                drawCircle(
                    color = colorAcento.copy(alpha = 0.05f),
                    radius = size.width * 0.24f,
                    center = Offset(size.width * 0.06f, size.height * 0.92f)
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (producto.destacado) {
                            Surface(
                                color = Color(0xFFFFF4D6),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.35f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        text = "Top",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }

                        if (producto.categoria.isNotBlank()) {
                            Surface(
                                color = colorAcento.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = producto.categoria.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorAcento,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = fondoStock,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colorStock.copy(alpha = 0.25f))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(colorStock))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                textoStock,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorStock
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.20f))
                    ) {
                        ImagenProducto(producto.imagen)
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = producto.nombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsSecundario,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = dinero(producto.precio),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = colorAcento
                            )
                            Text(
                                text = "c/u",
                                style = MaterialTheme.typography.labelSmall,
                                color = TsNeutro,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        if (mejorOferta != null) {
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                color = Color(0xFF0A876C).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Oferta ${mejorOferta.minQty}+ unid: ${dinero(mejorOferta.precioEfectivo)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0A876C),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        onClick = onVerDetalle,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.30f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = colorAcento,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (mejorOferta != null) "Ver Ficha" else "Ver Ficha Técnica",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colorAcento
                            )
                        }
                    }

                    Button(
                        onClick = onAgregarRapido,
                        enabled = habilitado,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorAcento,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Agregar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ModalDetalleProducto(
    producto: Producto,
    onDismiss: () -> Unit,
    onAgregar: (Int) -> Unit
) {
    var cantidad by remember { mutableIntStateOf(1) }
    val tramos = producto.precioVolumen
        .filter { it.precioEfectivo > 0.0 && it.minQty > 1 && it.precioEfectivo < producto.precio }
        .sortedBy { it.minQty }
    val (colorAcento, fondoClaro, _) = coloresPorCategoria(producto.categoria)
    val (textoStock, fondoStock, colorStock) = estadoStock(producto)

    val tramoActivo = tramos.lastOrNull { cantidad >= it.minQty }
    val precioUnitarioActual = tramoActivo?.precioEfectivo ?: producto.precio
    val tieneDescuentoActivo = tramoActivo != null
    val verdeEsmeralda = Color(0xFF0A876C)
    val specs = producto.specs.filter { it.label.isNotBlank() && it.value.isNotBlank() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            border = BorderStroke(1.5.dp, colorAcento.copy(alpha = 0.30f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(fondoClaro, Color.White)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = colorAcento.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = producto.categoria.ifBlank { "Hardware" }.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = colorAcento,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                onClick = onDismiss,
                                shape = CircleShape,
                                color = TsSuperficieSuave,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = TsSecundario,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.White,
                                shadowElevation = 3.dp,
                                border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.22f))
                            ) {
                                ImagenProducto(producto.imagen)
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = producto.nombre,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TsSecundario
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "SKU: ${producto.sku}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TsNeutro
                                )
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    color = fondoStock,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Row(
                                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(Modifier.size(6.dp).clip(CircleShape).background(colorStock))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            textoStock,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = colorStock
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(18.dp)) {
                    if (producto.descripcionCorta.isNotBlank()) {
                        Text(
                            text = "Descripción del equipo",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TsSecundario
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = producto.descripcionCorta,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TsNeutro
                        )
                        Spacer(Modifier.height(14.dp))
                    }

                    if (specs.isNotEmpty()) {
                        Text(
                            text = "Especificaciones Técnicas",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TsSecundario
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            specs.forEach { spec ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = fondoClaro.copy(alpha = 0.65f),
                                    border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.16f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = spec.label,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TsNeutro,
                                            modifier = Modifier.weight(0.45f)
                                        )
                                        Text(
                                            text = spec.value,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TsSecundario,
                                            textAlign = TextAlign.End,
                                            modifier = Modifier.weight(0.55f)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    if (tramos.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF3F7FF),
                            border = BorderStroke(1.dp, colorAcento.copy(alpha = 0.22f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "OFERTAS POR VOLUMEN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = colorAcento
                                    )
                                    if (tieneDescuentoActivo) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = verdeEsmeralda,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "Oferta aplicada",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = verdeEsmeralda
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    tramos.forEach { tramo ->
                                        val seleccionado = tramoActivo == tramo
                                        Surface(
                                            onClick = { cantidad = tramo.minQty },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (seleccionado) colorAcento else Color.White,
                                            border = BorderStroke(
                                                1.dp,
                                                if (seleccionado) colorAcento else colorAcento.copy(alpha = 0.25f)
                                            )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "${tramo.minQty}+ unid: ",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (seleccionado) Color.White.copy(alpha = 0.9f) else TsNeutro
                                                )
                                                Text(
                                                    text = dinero(tramo.precioEfectivo),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (seleccionado) Color.White else colorAcento
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "Precio unitario",
                                style = MaterialTheme.typography.labelSmall,
                                color = TsNeutro
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = dinero(precioUnitarioActual),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (tieneDescuentoActivo) verdeEsmeralda else colorAcento
                                )
                                if (tieneDescuentoActivo) {
                                    Text(
                                        text = dinero(producto.precio),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TsNeutro,
                                        textDecoration = TextDecoration.LineThrough,
                                        modifier = Modifier.padding(bottom = 3.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = TsSuperficieSuave,
                            border = BorderStroke(1.dp, TsBorde)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (cantidad > 1) cantidad-- },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Menos", tint = TsSecundario)
                                }
                                Text(
                                    text = "$cantidad",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TsSecundario,
                                    modifier = Modifier.widthIn(min = 28.dp),
                                    textAlign = TextAlign.Center
                                )
                                IconButton(
                                    onClick = { cantidad++ },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Más", tint = colorAcento)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { onAgregar(cantidad) },
                        enabled = producto.stock > 0 || producto.disponibilidad == "bajo_pedido",
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tieneDescuentoActivo) verdeEsmeralda else colorAcento,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Agregar $cantidad al Lote (${dinero(precioUnitarioActual * cantidad)})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun coloresPorCategoria(categoria: String): Triple<Color, Color, Color> {
    val cat = categoria.lowercase()
    return when {
        cat.contains("disco") || cat.contains("ssd") || cat.contains("almacenamiento") ->
            Triple(Color(0xFF0A876C), Color(0xFFDDF7EF), Color(0xFFF2FBF8))
        cat.contains("cpu") || cat.contains("procesador") || cat.contains("ram") || cat.contains("memoria") ->
            Triple(Color(0xFF5925DC), Color(0xFFEDE9FE), Color(0xFFF6F4FF))
        cat.contains("licencia") || cat.contains("os") || cat.contains("software") ->
            Triple(Color(0xFFB45309), Color(0xFFFEF3C7), Color(0xFFFFFBEB))
        else ->
            Triple(Color(0xFF155EEF), Color(0xFFDCE8FF), Color(0xFFF3F7FF))
    }
}

private fun estadoStock(p: Producto): Triple<String, Color, Color> = when {
    p.disponibilidad == "bajo_pedido" -> Triple("Bajo pedido", TsAvisoFondo, TsAvisoTexto)
    p.stock <= 0 -> Triple("Sin stock", TsErrorFondo, TsErrorTexto)
    p.stock <= 5 -> Triple("Últimas ${p.stock} unid.", TsErrorFondo, TsErrorTexto)
    p.disponibilidad == "48h" -> Triple("Entrega 48 h (${p.stock}u)", TsInfoFondo, TsInfoTexto)
    else -> Triple("Stock (${p.stock}u)", Color(0xFFD1FAE5), Color(0xFF0E7C66))
}

@Composable
private fun BarraLote(unidades: Int, subtotal: Double, onVerLote: () -> Unit) {
    val azulNoche = Color(0xFF0A1931)
    val cianPulso = Color(0xFF00D2FF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Surface(
            onClick = onVerLote,
            shape = RoundedCornerShape(20.dp),
            color = azulNoche,
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.40f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = cianPulso,
                            contentColor = azulNoche
                        ) {
                            Text(
                                "$unidades",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                ) {
                    IconoEnCaja(
                        Icons.Outlined.Inventory2,
                        tamano = 44.dp,
                        fondo = Color.White.copy(alpha = 0.14f),
                        tinte = Color.White
                    )
                }

                Spacer(Modifier.width(14.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Mis Productos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Subtotal: ${dinero(subtotal)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = cianPulso
                    )
                }

                Button(
                    onClick = onVerLote,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TsPrimario,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Ver Cotización",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(16.dp))
                }
            }
        }
    }
}