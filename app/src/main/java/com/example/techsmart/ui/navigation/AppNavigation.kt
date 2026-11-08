package com.example.techsmart.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.techsmart.data.repository.AuthRepository
import com.example.techsmart.ui.auth.LoginScreen
import com.example.techsmart.ui.auth.RegisterScreen
import com.example.techsmart.ui.cart.CartScreen
import com.example.techsmart.ui.cart.CartViewModel
import com.example.techsmart.ui.catalog.CatalogScreen
import com.example.techsmart.ui.catalog.CatalogViewModel
import com.example.techsmart.ui.home.HomeScreen
import com.example.techsmart.ui.rma.RmaScreen
import com.example.techsmart.ui.rma.RmaViewModel

@Composable
fun AppNavigation(
    catalogViewModel: CatalogViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    rmaViewModel: RmaViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authRepo = remember { AuthRepository() }

    // Si ya hay sesión guardada, se entra directo a Inicio
    val startDestination =
        if (authRepo.isLoggedIn) Screen.Inicio.route else Screen.AuthRoutes.LOGIN

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // La barra inferior solo se muestra en las pantallas principales (no en login/registro)
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    val cerrarSesion = {
        authRepo.logout()
        // Que el carrito y los reclamos no pasen a la siguiente persona que use el dispositivo
        cartViewModel.vaciar()
        rmaViewModel.vaciar()
        navController.navigate(Screen.AuthRoutes.LOGIN) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Inicio.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.AuthRoutes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.AuthRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onGoToRegister = { navController.navigate(Screen.AuthRoutes.REGISTER) }
                )
            }
            composable(Screen.AuthRoutes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.AuthRoutes.LOGIN) { inclusive = true }
                        }
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }

            composable(Screen.Inicio.route) {
                HomeScreen(
                    onIrACatalogo = { navController.navigate(Screen.Catalog.route) },
                    onIrACotizaciones = { navController.navigate(Screen.Cart.route) },
                    onIrAReclamos = { navController.navigate(Screen.Rma.route) },
                    onCerrarSesion = cerrarSesion
                )
            }
            composable(Screen.Catalog.route) {
                CatalogScreen(
                    viewModel = catalogViewModel,
                    onAgregarAlCarrito = { producto, cantidad ->
                        cartViewModel.agregarProducto(producto, cantidad)
                    }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = cartViewModel,
                    onIrACatalogo = {
                        navController.navigate(Screen.Catalog.route) {
                            popUpTo(Screen.Inicio.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Rma.route) {
                RmaScreen(viewModel = rmaViewModel)
            }
        }
    }
}