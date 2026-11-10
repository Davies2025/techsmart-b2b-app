package com.example.techsmart.ui.compra

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.ShoppingCartCheckout
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.domain.pago.ValidacionPago
import com.example.techsmart.ui.cart.CartViewModel
import com.example.techsmart.ui.cart.DatosCompra
import com.example.techsmart.ui.components.*
import com.example.techsmart.ui.theme.*

/** Datos del formulario de checkout. El número de tarjeta y el CVV solo viven en memoria. */
private data class FormCheckout(
    val tipoCliente: String = Cotizacion.TIPO_EMPRESA,
    val razonSocial: String = "",
    val documento: String = "",
    val contacto: String = "",
    val telefono: String = "",
    val correo: String = "",
    val metodo: String = Cotizacion.PAGO_TARJETA,
    val numero: String = "",
    val vencimiento: String = "",
    val cvv: String = "",
    val titular: String = ""
)

private fun formDesdePerfil(perfil: Usuario?): FormCheckout {
    if (perfil == null) return FormCheckout()
    val esEmpresa = perfil.esEmpresa
    return FormCheckout(
        tipoCliente = if (esEmpresa) Cotizacion.TIPO_EMPRESA else Cotizacion.TIPO_PARTICULAR,
        razonSocial = perfil.razonSocial.ifBlank { perfil.nombreContacto },
        documento = perfil.nit.takeUnless { it.equals("Consumidor Final", ignoreCase = true) }.orEmpty(),
        contacto = perfil.nombreContacto,
        telefono = perfil.telefono,
        correo = perfil.correo
    )
}

@Composable
fun CheckoutScreen(
    viewModel: CartViewModel,
    onVolver: () -> Unit,
    onCompraConfirmada: (folio: String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var form by remember(state.perfil) { mutableStateOf(formDesdePerfil(state.perfil)) }
    var mostrarErrores by remember { mutableStateOf(false) }

    val azulReal = Color(0xFF155EEF)
    val verdeEsmeralda = Color(0xFF0A876C)

    LaunchedEffect(Unit) { viewModel.cargarPerfil() }
    LaunchedEffect(state.compraConfirmada) {
        state.compraConfirmada?.let { folio ->
            viewModel.consumirCompraConfirmada()
            onCompraConfirmada(folio)
        }
    }
    LaunchedEffect(state.error) {
        state.error?.let {
            viewModel.consumirError()
            snackbar.showSnackbar(it)
        }
    }

    val esEmpresa = form.tipoCliente == Cotizacion.TIPO_EMPRESA
    val total = state.totales.total

    // ---------- Validaciones ----------
    val errores = buildMap {
        if (form.razonSocial.isBlank()) put("razon", "Escribe el nombre o la razón social.")
        ValidacionPago.errorDocumentoFiscal(form.documento, esEmpresa)?.let { put("doc", it) }
        if (form.telefono.isBlank()) put("tel", "Escribe un teléfono de contacto.")
        if (!form.correo.contains("@")) put("correo", "Escribe un correo válido.")
        when (form.metodo) {
            Cotizacion.PAGO_TARJETA -> {
                ValidacionPago.errorNumero(form.numero)?.let { put("numero", it) }
                ValidacionPago.errorVencimiento(form.vencimiento)?.let { put("venc", it) }
                ValidacionPago.errorCvv(form.cvv, form.numero)?.let { put("cvv", it) }
                ValidacionPago.errorTitular(form.titular)?.let { put("titular", it) }
            }
            Cotizacion.PAGO_CREDITO ->
                ValidacionPago.errorCredito(total, state.lineaCredito)?.let { put("credito", it) }
        }
    }
    fun error(clave: String) = if (mostrarErrores) errores[clave] else null

    val pagar = {
        mostrarErrores = true
        if (errores.isEmpty()) {
            val referencia = when (form.metodo) {
                Cotizacion.PAGO_TARJETA ->
                    "${ValidacionPago.marca(form.numero)?.nombre ?: "Tarjeta"} •••• ${ValidacionPago.ultimos4(form.numero)}"
                Cotizacion.PAGO_TRANSFERENCIA -> "Transferencia bancaria"
                else -> "Crédito Techsmart 30 días"
            }
            viewModel.comprar(
                DatosCompra(
                    tipoCliente = form.tipoCliente,
                    razonSocial = form.razonSocial,
                    documentoFiscal = form.documento,
                    contacto = form.contacto.ifBlank { form.razonSocial },
                    telefono = form.telefono,
                    correo = form.correo,
                    metodoPago = form.metodo,
                    pagoReferencia = referencia
                )
            )
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

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilaVolver("Volver al carrito", onVolver) {
                    EtiquetaMono("Pago simulado", fondo = TsAvisoFondo, color = TsAvisoTexto)
                }

                if (state.items.isEmpty() && !state.isSubmitting) {
                    MensajeVacio("Tu carrito está vacío.")
                    return@Box
                }

                HeaderCheckout()

                ResumenPedido(
                    articulos = state.items.size,
                    total = total,
                    lineas = state.items.map { "${it.cantidad} × ${it.nombreMostrado}" to it.subtotalMostrado }
                )

                // ---------- Facturación (Rediseño Premium) ----------
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(TsPrimario, Color(0xFF00C2FF))
                                    )
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Datos de Facturación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsSecundario,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = TsPrimario.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, TsPrimario.copy(alpha = 0.22f))
                        ) {
                            Text(
                                text = if (esEmpresa) Cotizacion.DOC_CCF else Cotizacion.DOC_CONSUMIDOR_FINAL,
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FuenteMono,
                                fontWeight = FontWeight.ExtraBold,
                                color = TsPrimario,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 4.dp,
                        border = BorderStroke(1.2.dp, TsPrimario.copy(alpha = 0.20f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            SelectorSegmentado(
                                opciones = listOf(Cotizacion.TIPO_EMPRESA, Cotizacion.TIPO_PARTICULAR),
                                seleccion = form.tipoCliente,
                                onSeleccion = { form = form.copy(tipoCliente = it) }
                            )
                            Spacer(Modifier.height(16.dp))
                            CampoValidado(
                                if (esEmpresa) "Razón social" else "Nombre completo",
                                form.razonSocial, Icons.Outlined.Business, error("razon")
                            ) { form = form.copy(razonSocial = it) }
                            CampoValidado(
                                if (esEmpresa) "NIT (0614-010190-101-1)" else "DUI (opcional, 12345678-9)",
                                form.documento, Icons.Outlined.Badge, error("doc"),
                                teclado = KeyboardType.Number,
                                ayuda = if (!esEmpresa && form.documento.isBlank()) "Sin DUI se factura a Consumidor Final." else null,
                                formatear = { formatearDocumento(it, esEmpresa) }
                            ) { form = form.copy(documento = it) }
                            CampoValidado("Teléfono", form.telefono, Icons.Outlined.Phone, error("tel"), KeyboardType.Phone) {
                                form = form.copy(telefono = it)
                            }
                            CampoValidado("Correo para la factura", form.correo, Icons.Outlined.Email, error("correo"), KeyboardType.Email) {
                                form = form.copy(correo = it)
                            }
                        }
                    }
                }

                // ---------- Método de pago (Rediseño Estilo Tarjetas Premium) ----------
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(verdeEsmeralda, TsPrimario)
                                    )
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Método de Pago",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TsSecundario,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = verdeEsmeralda.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, verdeEsmeralda.copy(alpha = 0.22f))
                        ) {
                            Text(
                                text = "3 OPCIONES",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FuenteMono,
                                fontWeight = FontWeight.ExtraBold,
                                color = verdeEsmeralda,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OpcionPago(
                    seleccionada = form.metodo == Cotizacion.PAGO_TARJETA,
                    titulo = "Tarjeta de crédito o débito",
                    descripcion = "Visa, Mastercard o American Express",
                    icono = Icons.Outlined.CreditCard,
                    etiquetas = listOf("VISA", "MC", "AMEX"),
                    onClick = { form = form.copy(metodo = Cotizacion.PAGO_TARJETA) }
                ) {
                    FormularioTarjeta(form, ::error) { form = it }
                }

                OpcionPago(
                    seleccionada = form.metodo == Cotizacion.PAGO_TRANSFERENCIA,
                    titulo = "Transferencia bancaria",
                    descripcion = "El pedido queda pendiente hasta confirmar el pago",
                    icono = Icons.Outlined.AccountBalance,
                    etiquetas = emptyList(),
                    onClick = { form = form.copy(metodo = Cotizacion.PAGO_TRANSFERENCIA) }
                ) {
                    CajaSuave {
                        Text(
                            "Tu pedido se registra como \"Pendiente de Pago\" y tienes 3 días para transferir. " +
                                    "Un administrador lo confirma al recibir el pago y entonces pasa a bodega.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TsSecundario
                        )
                    }
                }

                val creditoSuficiente = state.lineaCredito > 0 && total <= state.lineaCredito
                OpcionPago(
                    seleccionada = form.metodo == Cotizacion.PAGO_CREDITO,
                    titulo = "Crédito Techsmart 30 días",
                    descripcion = "Se carga a tu línea de crédito",
                    icono = Icons.Outlined.VerifiedUser,
                    etiquetas = listOf(if (creditoSuficiente) "Disponible" else "Insuficiente"),
                    etiquetaOk = creditoSuficiente,
                    onClick = { form = form.copy(metodo = Cotizacion.PAGO_CREDITO) }
                ) {
                    CajaSuave {
                        FilaMonto("Línea disponible", dinero(state.lineaCredito))
                        FilaMonto("Total de la compra", dinero(total))
                    }
                    error("credito")?.let { TextoError(it) }
                }

                // ---------- Aviso y botón ----------
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = TsAvisoFondo,
                    border = BorderStroke(1.dp, TsAvisoTexto.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Outlined.Info, null, tint = TsAvisoTexto, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Proyecto académico: el pago es simulado y no se realiza ningún cargo. Solo se valida el " +
                                    "formato de los datos (prueba con 4111 1111 1111 1111). No se guarda el número de la tarjeta.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TsAvisoTexto,
                            lineHeight = 16.sp
                        )
                    }
                }

                Button(
                    onClick = pagar,
                    enabled = !state.isSubmitting,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = verdeEsmeralda,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Icon(Icons.Default.Lock, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (form.metodo == Cotizacion.PAGO_TRANSFERENCIA) "Confirmar Pedido ${dinero(total)}"
                            else "Confirmar y Pagar ${dinero(total)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (mostrarErrores && errores.isNotEmpty()) {
                    TextoError("Revisa los campos marcados.")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Partes
// ---------------------------------------------------------------------------

@Composable
private fun HeaderCheckout() {
    val azulNoche = Color(0xFF0A1931)
    val azulReal = Color(0xFF155EEF)
    val cianPulso = Color(0xFF00D2FF)
    val verdeMenta = Color(0xFF34D399)

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
                val cy = size.height * 0.22f
                drawCircle(
                    color = cianPulso.copy(alpha = 0.14f),
                    radius = size.width * 0.28f,
                    center = Offset(cx, cy)
                )
            }

            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = cianPulso.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, cianPulso.copy(alpha = 0.45f)),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCartCheckout,
                                contentDescription = null,
                                tint = cianPulso,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "PASARELA SEGURA B2B",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = cianPulso
                        )
                        Text(
                            text = "Checkout y Pago",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.24f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PasoFlujoCheckout("1", "Carrito", completado = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .height(1.5.dp)
                                .background(verdeMenta)
                        )
                        PasoFlujoCheckout("2", "Pago", activo = true, colorActivo = verdeMenta)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 6.dp)
                                .height(1.5.dp)
                                .background(Color.White.copy(alpha = 0.25f))
                        )
                        PasoFlujoCheckout("3", "Recibo", activo = false, colorActivo = cianPulso)
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoFlujoCheckout(
    numero: String,
    titulo: String,
    activo: Boolean = false,
    completado: Boolean = false,
    colorActivo: Color = Color(0xFF34D399)
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(
                    when {
                        completado -> Color(0xFF34D399)
                        activo -> colorActivo
                        else -> Color.White.copy(alpha = 0.18f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (completado) "✓" else numero,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontFamily = FuenteMono,
                fontWeight = FontWeight.ExtraBold,
                color = if (completado || activo) Color(0xFF0A1931) else Color.White.copy(alpha = 0.75f)
            )
        }
        Spacer(Modifier.width(5.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            fontWeight = if (activo || completado) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (activo || completado) Color.White else Color.White.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun ResumenPedido(articulos: Int, total: Double, lineas: List<Pair<String, Double>>) {
    var abierto by remember { mutableStateOf(false) }
    val azulNoche = Color(0xFF0A1931)
    val cianPulso = Color(0xFF00D2FF)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = azulNoche,
        shadowElevation = 6.dp,
        border = BorderStroke(1.2.dp, cianPulso.copy(alpha = 0.32f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "$articulos ${if (articulos == 1) "artículo seleccionado" else "artículos seleccionados"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = cianPulso
                    )
                    Spacer(Modifier.height(2.dp))
                    Text("Total con IVA (13%)", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
                }
                TextButton(
                    onClick = { abierto = !abierto },
                    colors = ButtonDefaults.textButtonColors(contentColor = cianPulsonSi(cianPulso))
                ) {
                    Text(if (abierto) "Ocultar" else "Ver detalle", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(2.dp))
                    Icon(if (abierto) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, null, Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    dinero(total),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = cianPulso
                )
                Text(" USD", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 4.dp))
            }
            if (abierto) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    lineas.forEach { (texto, monto) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(texto, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
                            Text(dinero(monto), style = MaterialTheme.typography.bodySmall, fontFamily = FuenteMono, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

private fun cianPulsonSi(color: Color) = color

@Composable
private fun OpcionPago(
    seleccionada: Boolean,
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    etiquetas: List<String>,
    etiquetaOk: Boolean = true,
    onClick: () -> Unit,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val verdeEsmeralda = Color(0xFF0A876C)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = if (seleccionada) Color(0xFFF0FBF7) else Color.White,
        shadowElevation = if (seleccionada) 4.dp else 2.dp,
        border = BorderStroke(
            if (seleccionada) 1.8.dp else 1.dp,
            if (seleccionada) verdeEsmeralda else TsPrimario.copy(alpha = 0.20f)
        ),
        modifier = Modifier.fillMaxWidth().animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = seleccionada,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(selectedColor = verdeEsmeralda)
                )
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = TsSecundario)
                    Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TsNeutro)
                    if (etiquetas.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            etiquetas.forEach {
                                if (etiquetaOk) EtiquetaMono(it, fondo = TsSuperficieSuave, color = TsSecundario)
                                else EtiquetaMono(it, fondo = TsErrorFondo, color = TsErrorTexto)
                            }
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (seleccionada) verdeEsmeralda.copy(alpha = 0.15f) else TsSuperficieSuave,
                    border = BorderStroke(1.dp, if (seleccionada) verdeEsmeralda.copy(alpha = 0.3f) else Color.Transparent),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icono, null, tint = if (seleccionada) verdeEsmeralda else TsNeutro, modifier = Modifier.size(20.dp))
                    }
                }
            }
            if (seleccionada) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = verdeEsmeralda.copy(alpha = 0.2f))
                Spacer(Modifier.height(14.dp))
                contenido()
            }
        }
    }
}

@Composable
private fun FormularioTarjeta(
    form: FormCheckout,
    error: (String) -> String?,
    onCambio: (FormCheckout) -> Unit
) {
    val marca = ValidacionPago.marca(form.numero)
    CampoValidado(
        "Número de tarjeta",
        form.numero,
        Icons.Outlined.CreditCard,
        error("numero"),
        KeyboardType.Number,
        ayuda = marca?.nombre,
        formatear = ValidacionPago::formatearNumero
    ) { onCambio(form.copy(numero = it)) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(Modifier.weight(1f)) {
            CampoValidado(
                "Vence (MM/AA)", form.vencimiento, null, error("venc"), KeyboardType.Number,
                formatear = ValidacionPago::formatearVencimiento
            ) { onCambio(form.copy(vencimiento = it)) }
        }
        Column(Modifier.weight(1f)) {
            CampoValidado(
                "CVV", form.cvv, null, error("cvv"), KeyboardType.NumberPassword,
                oculto = true
            ) { onCambio(form.copy(cvv = it.filter(Char::isDigit).take(4))) }
        }
    }
    CampoValidado("Titular (como aparece en la tarjeta)", form.titular, Icons.Outlined.Person, error("titular")) {
        onCambio(form.copy(titular = it.uppercase()))
    }
}

@Composable
private fun CampoValidado(
    etiqueta: String,
    valor: String,
    icono: ImageVector?,
    error: String?,
    teclado: KeyboardType = KeyboardType.Text,
    ayuda: String? = null,
    oculto: Boolean = false,
    formatear: ((String) -> String)? = null,
    onCambio: (String) -> Unit
) {
    var campo by remember { mutableStateOf(TextFieldValue(valor, TextRange(valor.length))) }
    if (campo.text != valor) campo = TextFieldValue(valor, TextRange(valor.length))

    EtiquetaCampo(etiqueta)
    TextField(
        value = campo,
        onValueChange = { nuevo ->
            if (formatear == null) {
                campo = nuevo
                onCambio(nuevo.text)
            } else {
                val texto = formatear(nuevo.text)
                campo = TextFieldValue(texto, TextRange(texto.length))
                onCambio(texto)
            }
        },
        leadingIcon = icono?.let { { Icon(it, null, tint = if (error != null) TsErrorTexto else TsNeutro) } },
        singleLine = true,
        isError = error != null,
        visualTransformation = if (oculto) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TsSuperficieSuave,
            unfocusedContainerColor = TsSuperficieSuave,
            errorContainerColor = TsErrorFondo,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    )
    when {
        error != null -> TextoError(error)
        ayuda != null -> Text(
            ayuda,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TsPrimario,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun TextoError(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        color = TsErrorTexto,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 4.dp)
    )
}

private fun formatearDocumento(texto: String, esEmpresa: Boolean): String {
    val d = texto.filter(Char::isDigit)
    return if (esEmpresa) {
        val p = d.take(14)
        buildString {
            p.forEachIndexed { i, c ->
                if (i == 4 || i == 10 || i == 13) append('-')
                append(c)
            }
        }
    } else {
        val p = d.take(9)
        if (p.length > 8) p.take(8) + "-" + p.drop(8) else p
    }
}