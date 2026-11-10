package com.example.techsmart.ui.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Colores del widget de la web (estilo WhatsApp)
private val VerdeEncabezado = Color(0xFF075E54)
private val VerdeEnLinea = Color(0xFF25D366)
private val FondoChat = Color(0xFFEFEAE2)
private val BurbujaUsuario = Color(0xFFD9FDD3)
private val BurbujaBot = Color(0xFFFFFFFF)
private val TextoBurbuja = Color(0xFF111B21)
private val HoraBurbuja = Color(0xFF667781)

/**
 * Asesor IA en una hoja que cubre casi toda la pantalla.
 * Se abre desde la barra superior en cualquier vista; la conversación se conserva al cerrarla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSheet(viewModel: ChatViewModel, onCerrar: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val hoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = hoja,
        dragHandle = null,
        containerColor = FondoChat,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .imePadding()
        ) {
            Encabezado(onReiniciar = viewModel::reiniciar, onCerrar = onCerrar)
            ListaMensajes(state, Modifier.weight(1f))
            CampoMensaje(enviando = state.escribiendo, onEnviar = viewModel::enviar)
        }
    }
}

@Composable
private fun Encabezado(onReiniciar: () -> Unit, onCerrar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(VerdeEncabezado)
            .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.SmartToy, null, tint = Color.White)
            }
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(VerdeEncabezado)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(VerdeEnLinea)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "Techsmart B2B Asesor IA",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "En línea • Respuestas al instante",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        IconButton(onClick = onReiniciar) {
            Icon(Icons.Outlined.Refresh, contentDescription = "Nueva conversación", tint = Color.White)
        }
        IconButton(onClick = onCerrar) {
            Icon(Icons.Default.Close, contentDescription = "Cerrar chat", tint = Color.White)
        }
    }
}

@Composable
private fun ListaMensajes(state: ChatUiState, modifier: Modifier) {
    val lista = rememberLazyListState()
    val totalItems = state.mensajes.size + 1 + if (state.escribiendo) 1 else 0

    // Auto-scroll al final cada vez que llega un mensaje o aparece "escribiendo"
    LaunchedEffect(state.mensajes.size, state.escribiendo) {
        lista.animateScrollToItem(totalItems - 1)
    }

    LazyColumn(
        state = lista,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(color = Color(0xFFFFF5C4), shape = RoundedCornerShape(8.dp)) {
                    Text(
                        "Asistente con IA · revisa precios y stock en el catálogo",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF54656F),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        items(state.mensajes, key = { it.id }) { Burbuja(it) }
        if (state.escribiendo) {
            item(key = "escribiendo") { Escribiendo() }
        }
    }
}

@Composable
private fun Burbuja(mensaje: ChatMessage) {
    val esUsuario = mensaje.sender == "user"
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (esUsuario) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (esUsuario) BurbujaUsuario else BurbujaBot,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (esUsuario) 12.dp else 2.dp,
                bottomEnd = if (esUsuario) 2.dp else 12.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp)) {
                Text(
                    textoConFormato(mensaje.text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoBurbuja
                )
                Text(
                    mensaje.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = HoraBurbuja,
                    modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                )
            }
        }
    }
}

/** Indicador "escribiendo…": tres puntos que suben y bajan. */
@Composable
private fun Escribiendo() {
    val transicion = rememberInfiniteTransition(label = "escribiendo")
    Row(Modifier.fillMaxWidth()) {
        Surface(
            color = BurbujaBot,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 2.dp),
            shadowElevation = 1.dp
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    val desplazamiento by transicion.animateFloat(
                        initialValue = 0f,
                        targetValue = 0f,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 1200
                                0f at 0 + i * 150
                                -6f at 200 + i * 150
                                0f at 400 + i * 150
                            }
                        ),
                        label = "punto$i"
                    )
                    Box(
                        Modifier
                            .offset(y = desplazamiento.dp)
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(HoraBurbuja)
                    )
                }
            }
        }
    }
}

@Composable
private fun CampoMensaje(enviando: Boolean, onEnviar: (String) -> Unit) {
    var texto by rememberSaveable { mutableStateOf("") }
    val puedeEnviar = texto.isNotBlank() && !enviando
    val enviar = {
        if (puedeEnviar) {
            onEnviar(texto)
            texto = ""
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F2F5))
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = texto,
            onValueChange = { texto = it },
            placeholder = { Text("Escribe tu consulta de hardware…") },
            maxLines = 4,
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { enviar() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        FilledIconButton(
            onClick = enviar,
            enabled = puedeEnviar,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = VerdeEncabezado,
                disabledContainerColor = Color(0xFFB8C4C2)
            ),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = Color.White)
        }
    }
}

/**
 * Convierte el formato que usa el asesor (estilo WhatsApp/Markdown) en texto con estilo:
 * **negrita** y viñetas "* " o "- " al inicio de línea como "• ".
 */
internal fun textoConFormato(texto: String): AnnotatedString = buildAnnotatedString {
    val lineas = texto.lines().map { linea ->
        val sinSangria = linea.trimStart()
        if (sinSangria.startsWith("* ") || sinSangria.startsWith("- ")) "• " + sinSangria.drop(2) else linea
    }
    val normalizado = lineas.joinToString("\n")
    val partes = normalizado.split("**")
    partes.forEachIndexed { i, parte ->
        // Las partes impares están entre ** ** (si hay un ** sin cerrar, el resto queda en negrita)
        if (i % 2 == 1) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(parte) }
        else append(parte)
    }
}
