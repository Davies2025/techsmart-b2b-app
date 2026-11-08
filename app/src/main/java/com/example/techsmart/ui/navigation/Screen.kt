package com.example.techsmart.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Inicio : Screen("inicio", "Inicio", Icons.Default.Home)
    data object Catalog : Screen("catalog", "Catálogo", Icons.Default.List)
    data object Cart : Screen("cart", "Cotizaciones", Icons.Default.ShoppingCart)
    data object Rma : Screen("rma", "Reclamos", Icons.Default.Build)

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