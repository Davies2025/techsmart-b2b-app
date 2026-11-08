package com.example.techsmart.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.techsmart.ui.components.IconoEnCaja
import com.example.techsmart.ui.components.LogoTechsmart
import com.example.techsmart.ui.components.TarjetaTs
import com.example.techsmart.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    onIrACatalogo: () -> Unit,
    onIrACotizaciones: () -> Unit,
    onIrAReclamos: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TsFondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ---------- Marca ----------
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = TsSuperficie,
            shadowElevation = 4.dp
        ) {
            LogoTechsmart(88.dp, Modifier.padding(12.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("Techsmart", style = MaterialTheme.typography.headlineMedium, color = TsSecundario)
        Text(
            "Soluciones de Hardware y Accesorios Tecnológicos",
            style = MaterialTheme.typography.bodyLarge,
            color = TsNeutro,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // ---------- Tarjeta principal: cotizaciones ----------
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(TsPrimario, TsPrimarioOscuro)))
                .padding(20.dp)
        ) {
            IconoEnCaja(
                Icons.Outlined.RequestQuote,
                tamano = 52.dp,
                fondo = Color.White.copy(alpha = 0.15f),
                tinte = Color.White
            )
            Spacer(Modifier.height(16.dp))
            Text("Solicitar Cotización", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(
                "Arma tu lote desde el catálogo con precios por volumen e IVA incluido, " +
                    "y da seguimiento a tus cotizaciones.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Precio por volumen", "IVA 13%").forEach {
                    Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            it,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onIrACotizaciones,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TsPrimario),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text("Crear Solicitud", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        AccesoSecundario(
            icono = Icons.Outlined.VerifiedUser,
            titulo = "Centro de Reclamos y Garantías",
            descripcion = "Reporta fallas de tus equipos y da seguimiento a tus casos RMA.",
            etiqueta = "RMA Digitalizado",
            colorPunto = TsPrimario,
            onClick = onIrAReclamos
        )
        Spacer(Modifier.height(12.dp))
        AccesoSecundario(
            icono = Icons.Outlined.Inventory2,
            titulo = "Explorar Catálogo",
            descripcion = "Productos, precios por volumen y disponibilidad en bodega.",
            etiqueta = "Inventario en línea",
            colorPunto = Color(0xFF0E7C66),
            onClick = onIrACatalogo
        )
    }
}

@Composable
private fun AccesoSecundario(
    icono: ImageVector,
    titulo: String,
    descripcion: String,
    etiqueta: String,
    colorPunto: Color,
    onClick: () -> Unit
) {
    TarjetaTs(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            IconoEnCaja(icono, tamano = 56.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = TsSecundario)
                Spacer(Modifier.height(4.dp))
                Text(descripcion, style = MaterialTheme.typography.bodySmall, color = TsNeutro)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(colorPunto))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = TsNeutro,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TsNeutro)
        }
    }
}
