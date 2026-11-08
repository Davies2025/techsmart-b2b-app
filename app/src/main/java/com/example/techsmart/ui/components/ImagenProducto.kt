package com.example.techsmart.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.techsmart.ui.theme.TsNeutro
import com.example.techsmart.ui.theme.TsSuperficieSuave

/** Foto del producto desde su URL, con ícono de reemplazo si no hay o no carga. */
@Composable
fun ImagenProducto(url: String, tamano: Dp = 84.dp) {
    val modificador = Modifier
        .size(tamano)
        .clip(RoundedCornerShape(12.dp))
        .background(TsSuperficieSuave)
    if (url.isBlank()) {
        Box(modificador, contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Inventory2, null, tint = TsNeutro)
        }
    } else {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modificador,
            loading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }
            },
            error = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Image, null, tint = TsNeutro)
                }
            }
        )
    }
}
