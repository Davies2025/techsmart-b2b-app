package com.example.techsmart.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * @param title texto de la barra inferior
 * @param subtitulo texto bajo "Techsmart" en la barra superior
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val subtitulo: String
) {
    data object Inicio : Screen("inicio", "Inicio", Icons.Outlined.Home, "Inicio")
    data object Catalog : Screen("catalog", "Catálogo", Icons.Outlined.Inventory2, "Catálogo Techsmart")
    data object Cart : Screen("cart", "Cotizaciones", Icons.Outlined.RequestQuote, "Cotizaciones")
    data object Rma : Screen("rma", "Reclamos", Icons.Outlined.SupportAgent, "Reclamos")

    object AuthRoutes {
        const val LOGIN = "login"
        const val REGISTER = "register"
    }
}

val bottomNavItems = listOf(
    Screen.Inicio,
    Screen.Catalog,
    Screen.Cart,
    Screen.Rma
)
