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

    // Flujo de compra ("Comprar ahora"). No están en la barra inferior: se marca "Cotizaciones".
    data object Carrito : Screen("carrito", "Carrito", Icons.Outlined.RequestQuote, "Carrito de Compras")
    data object Checkout : Screen("checkout", "Pago", Icons.Outlined.RequestQuote, "Checkout")
    // Historial de compras, cotizaciones y pagos (desde el menú de la cuenta)
    data object Historial : Screen("historial", "Historial", Icons.Outlined.RequestQuote, "Mi Historial")
    data object Pedido : Screen("pedido/{folio}", "Pedido", Icons.Outlined.RequestQuote, "Mi Pedido") {
        fun ruta(folio: String) = "pedido/$folio"
    }

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

/** Pantallas del flujo de compra (muestran las barras y marcan "Cotizaciones"). */
val pantallasCompra = listOf(Screen.Carrito, Screen.Checkout, Screen.Pedido)
