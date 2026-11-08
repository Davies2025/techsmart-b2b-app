package com.example.techsmart.ui.compra

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
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
        // "Consumidor Final" se representa con el campo vacío
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
    // Los errores se muestran después del primer intento de pagar
    var mostrarErrores by remember { mutableStateOf(false) }

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
        containerColor = TsFondo,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilaVolver("Volver al carrito", onVolver) {
                EtiquetaMono("Pago simulado", fondo = TsAvisoFondo, color = TsAvisoTexto)
            }

            if (state.items.isEmpty() && !state.isSubmitting) {
                MensajeVacio("Tu carrito está vacío.")
                return@Column
            }

            ResumenPedido(state.items.size, total, state.items.map { "${it.cantidad} × ${it.nombreMostrado}" to it.subtotalMostrado })

            // ---------- Facturación ----------
            TarjetaTs(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoEnCaja(Icons.AutoMirrored.Outlined.ReceiptLong, tamano = 40.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Datos de facturación", style = MaterialTheme.typography.titleSmall, color = TsSecundario)
                        Text(
                            if (esEmpresa) Cotizacion.DOC_CCF else Cotizacion.DOC_CONSUMIDOR_FINAL,
                            style = MaterialTheme.typography.labelSmall,
                            color = TsPrimario
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                SelectorSegmentado(
                    opciones = listOf(Cotizacion.TIPO_EMPRESA, Cotizacion.TIPO_PARTICULAR),
                    seleccion = form.tipoCliente,
                    onSeleccion = { form = form.copy(tipoCliente = it) }
                )
                Spacer(Modifier.height(12.dp))
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

            // ---------- Método de pago ----------
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Método de Pago",
                    style = MaterialTheme.typography.titleLarge,
                    color = TsSecundario,
                    modifier = Modifier.weight(1f)
                )
                Text("3 opciones", style = MaterialTheme.typography.labelMedium, color = TsNeutro)
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TsAvisoFondo)
                    .padding(12.dp)
            ) {
                Icon(Icons.Outlined.Info, null, tint = TsAvisoTexto, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Proyecto académico: el pago es simulado y no se realiza ningún cargo. Solo se valida el " +
                        "formato de los datos (prueba con 4111 1111 1111 1111). No se guarda el número de la tarjeta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TsAvisoTexto
                )
            }

            Button(
                onClick = pagar,
                enabled = !state.isSubmitting,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = TsSuperficie)
                } else {
                    Icon(Icons.Default.Lock, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (form.metodo == Cotizacion.PAGO_TRANSFERENCIA) "Confirmar Pedido ${dinero(total)}"
                        else "Confirmar y Pagar ${dinero(total)}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
            if (mostrarErrores && errores.isNotEmpty()) {
                TextoError("Revisa los campos marcados.")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Partes
// ---------------------------------------------------------------------------

@Composable
private fun ResumenPedido(articulos: Int, total: Double, lineas: List<Pair<String, Double>>) {
    var abierto by remember { mutableStateOf(false) }
    TarjetaTs(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$articulos ${if (articulos == 1) "artículo" else "artículos"}",
                style = MaterialTheme.typography.bodySmall,
                color = TsNeutro,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { abierto = !abierto }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text("Detalle", style = MaterialTheme.typography.bodySmall)
                Icon(if (abierto) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown, null)
            }
        }
        Text("Total con IVA (13%)", style = MaterialTheme.typography.bodySmall, color = TsNeutro)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(dinero(total), style = MaterialTheme.typography.headlineMedium, color = TsSecundario)
            Text(" USD", style = MaterialTheme.typography.labelMedium, color = TsPrimario, modifier = Modifier.padding(bottom = 4.dp))
        }
        if (abierto) {
            Spacer(Modifier.height(8.dp))
            CajaSuave { lineas.forEach { (texto, monto) -> FilaMonto(texto, dinero(monto)) } }
        }
    }
}

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
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (seleccionada) 2.dp else 1.dp, if (seleccionada) TsPrimario else TsBorde),
        colors = CardDefaults.cardColors(containerColor = TsSuperficie),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = seleccionada, onClick = onClick)
                Column(Modifier.weight(1f)) {
                    Text(titulo, style = MaterialTheme.typography.titleSmall, color = TsSecundario)
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
                Icon(icono, null, tint = TsPrimario)
            }
            if (seleccionada) {
                Spacer(Modifier.height(12.dp))
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
    // Si se indica, el texto se reformatea al escribir (espacios, "/" o guiones)
    // y el cursor se deja al final para que los dígitos no se desordenen.
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
            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            errorIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth()
    )
    when {
        error != null -> TextoError(error)
        ayuda != null -> Text(
            ayuda,
            style = MaterialTheme.typography.labelSmall,
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

/**
 * Pone los guiones mientras se escribe:
 * NIT 0614-010190-101-1 (14 dígitos) o DUI 12345678-9 (9 dígitos).
 */
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
