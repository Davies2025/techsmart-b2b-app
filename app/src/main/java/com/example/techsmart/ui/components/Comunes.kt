package com.example.techsmart.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

/** "$1,234.50" (el dólar es la moneda de El Salvador). */
fun dinero(valor: Double): String = String.format(Locale.US, "$%,.2f", valor)

/** Etiqueta de color para el estado de una cotización o reclamo. */
@Composable
fun EstadoChip(estado: String, modifier: Modifier = Modifier) {
    val (fondo, texto) = when (estado) {
        "Aprobada", "Despachada", "Cambio Aprobado", "Cerrado" ->
            Color(0xFFDCFCE7) to Color(0xFF166534)
        "Cancelada" -> Color(0xFFFEE2E2) to Color(0xFF991B1B)
        "Pendiente de Pago", "Recibido en Bodega", "En Diagnóstico Técnico" ->
            Color(0xFFFEF3C7) to Color(0xFF92400E)
        else -> Color(0xFFDBEAFE) to Color(0xFF1E40AF) // "En Revisión", "Reportado"
    }
    Surface(color = fondo, shape = RoundedCornerShape(50), modifier = modifier) {
        Text(
            estado,
            color = texto,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Fila "Etiqueta ........ valor" para desgloses. */
@Composable
fun FilaMonto(
    etiqueta: String,
    valor: String,
    destacado: Boolean = false,
    colorValor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val estilo = if (destacado) MaterialTheme.typography.titleMedium
        else MaterialTheme.typography.bodyMedium
        Text(etiqueta, style = estilo)
        Text(
            valor,
            style = estilo,
            color = colorValor,
            fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/** Texto centrado para listas vacías o estados de carga. */
@Composable
fun MensajeVacio(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(24.dp)
    )
}
