package com.example.techsmart.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.techsmart.ui.cart.CartViewModel
import com.example.techsmart.ui.catalog.CatalogViewModel
import com.example.techsmart.ui.rma.RmaViewModel
import com.example.techsmart.ui.test.*

@Composable
fun AppNavigation(
    catalogViewModel: CatalogViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    rmaViewModel: RmaViewModel = viewModel()
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Inicio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Inicio.route) {
                InicioTestScreen(
                    onIrACatalogo = { navController.navigate(Screen.Catalog.route) },
                    onIrACotizaciones = { navController.navigate(Screen.Cart.route) },
                    onIrAReclamos = { navController.navigate(Screen.Rma.route) }
                )
            }
            composable(Screen.Catalog.route) {
                CatalogTestScreen(
                    viewModel = catalogViewModel,
                    onAgregarAlCarrito = { producto, cantidad ->
                        cartViewModel.agregarProducto(producto, cantidad)
                    }
                )
            }
            composable(Screen.Cart.route) {
                CartTestScreen(viewModel = cartViewModel)
            }
            composable(Screen.Rma.route) {
                RmaTestScreen(viewModel = rmaViewModel)
            }
        }
    }
}