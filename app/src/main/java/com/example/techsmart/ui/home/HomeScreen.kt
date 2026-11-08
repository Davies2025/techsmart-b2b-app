package com.example.techsmart.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.techsmart.data.repository.AuthRepository
import com.example.techsmart.ui.navigation.Screen

@Composable
fun HomeScreen(
    onIrACatalogo: () -> Unit,
    onIrACotizaciones: () -> Unit,
    onIrAReclamos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val correo = remember { AuthRepository().currentEmail }

    val accesos = listOf(
        Triple(Screen.Catalog, "Consulta productos, precios y disponibilidad", onIrACatalogo),
        Triple(Screen.Cart, "Arma y revisa tus cotizaciones", onIrACotizaciones),
        Triple(Screen.Rma, "Reporta garantías y da seguimiento", onIrAReclamos)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            "Techsmart",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        if (!correo.isNullOrBlank()) {
            Text(
                "Sesión iniciada como $correo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))

        accesos.forEach { (screen, descripcion, onClick) ->
            ElevatedCard(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        screen.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(screen.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = onCerrarSesion,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar sesión")
        }
    }
}