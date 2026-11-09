package com.example.techsmart.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.techsmart.data.repository.AuthRepository
import com.example.techsmart.ui.auth.LoginScreen
import com.example.techsmart.ui.auth.RegisterScreen
import com.example.techsmart.ui.cart.CartScreen
import com.example.techsmart.ui.cart.CartViewModel
import com.example.techsmart.ui.catalog.CatalogScreen
import com.example.techsmart.ui.catalog.CatalogViewModel
import com.example.techsmart.ui.components.BarraSuperiorTechsmart
import com.example.techsmart.ui.compra.CarritoScreen
import com.example.techsmart.ui.compra.CheckoutScreen
import com.example.techsmart.ui.compra.PedidoScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.techsmart.ui.home.HomeScreen
import com.example.techsmart.ui.rma.RmaScreen
import com.example.techsmart.ui.rma.RmaViewModel
import com.example.techsmart.ui.theme.TsAzulSuave
import com.example.techsmart.ui.theme.TsNeutro
import com.example.techsmart.ui.theme.TsPrimario
import com.example.techsmart.ui.theme.TsSuperficie

@Composable
fun AppNavigation(
    catalogViewModel: CatalogViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel(),
    rmaViewModel: RmaViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authRepo = remember { AuthRepository() }
    val carrito by cartViewModel.uiState.collectAsStateWithLifecycle()

    // Si ya hay sesión guardada, se entra directo a Inicio
    val startDestination =
        if (authRepo.isLoggedIn) Screen.Inicio.route else Screen.AuthRoutes.LOGIN

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Barras superior e inferior solo en las pantallas principales (no en login/registro)
    val pantallaActual = (bottomNavItems + pantallasCompra).find { it.route == currentRoute }
    val enCompra = pantallasCompra.any { it.route == currentRoute }

    val irA: (Screen) -> Unit = { screen ->
        if (currentRoute != screen.route) {
            navController.navigate(screen.route) {
                popUpTo(Screen.Inicio.route) { saveState = true }
                launchSingleTop = true
                // Cotizaciones siempre abre su lista: no se restaura el carrito, el pago ni un pedido viejo
                restoreState = screen != Screen.Cart
            }
        }
    }

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
        topBar = {
            if (pantallaActual != null) {
                BarraSuperiorTechsmart(
                    subtitulo = pantallaActual.subtitulo,
                    correoSesion = authRepo.currentEmail,
                    onCerrarSesion = cerrarSesion
                )
            }
        },
        bottomBar = {
            if (pantallaActual != null) {
                NavigationBar(containerColor = TsSuperficie, tonalElevation = 0.dp) {
                    bottomNavItems.forEach { screen ->
                        val seleccionada = currentRoute == screen.route || (enCompra && screen == Screen.Cart)
                        NavigationBarItem(
                            icon = {
                                // La pestaña de cotizaciones muestra cuántas unidades hay en el lote
                                val unidades = carrito.items.sumOf { it.cantidad }
                                if (screen == Screen.Cart && unidades > 0) {
                                    BadgedBox(badge = { Badge { Text("$unidades") } }) {
                                        Icon(screen.icon, contentDescription = screen.title)
                                    }
                                } else {
                                    Icon(screen.icon, contentDescription = screen.title)
                                }
                            },
                            label = {
                                Text(
                                    screen.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = seleccionada,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = TsPrimario,
                                indicatorColor = TsPrimario,
                                unselectedIconColor = TsNeutro.copy(alpha = 0.6f),
                                unselectedTextColor = TsNeutro.copy(alpha = 0.6f)
                            ),
                            onClick = {
                                // Desde el flujo de compra, "Cotizaciones" vuelve a la lista
                                if (enCompra && screen == Screen.Cart) {
                                    navController.popBackStack(Screen.Cart.route, inclusive = false)
                                } else {
                                    irA(screen)
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
                    onIrACatalogo = { irA(Screen.Catalog) },
                    onIrACotizaciones = { irA(Screen.Cart) },
                    onIrAReclamos = { irA(Screen.Rma) }
                )
            }
            composable(Screen.Catalog.route) {
                CatalogScreen(
                    viewModel = catalogViewModel,
                    unidadesEnLote = carrito.items.sumOf { it.cantidad },
                    subtotalLote = carrito.totales.subtotalNeto,
                    onAgregarAlCarrito = { producto, cantidad ->
                        cartViewModel.agregarProducto(producto, cantidad)
                    },
                    onVerLote = { irA(Screen.Cart) }
                )
            }
            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = cartViewModel,
                    onIrACatalogo = { irA(Screen.Catalog) },
                    onComprarAhora = { navController.navigate(Screen.Carrito.route) },
                    onVerPedido = { folio -> navController.navigate(Screen.Pedido.ruta(folio)) }
                )
            }

            // ---------- Flujo de compra ----------
            composable(Screen.Carrito.route) {
                CarritoScreen(
                    viewModel = cartViewModel,
                    onVolver = { navController.popBackStack() },
                    onIrACatalogo = { irA(Screen.Catalog) },
                    onContinuarPago = { navController.navigate(Screen.Checkout.route) }
                )
            }
            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    viewModel = cartViewModel,
                    onVolver = { navController.popBackStack() },
                    onCompraConfirmada = { folio ->
                        // Al volver atrás desde la confirmación se regresa a Cotizaciones, no al checkout
                        navController.navigate(Screen.Pedido.ruta(folio)) {
                            popUpTo(Screen.Cart.route)
                        }
                    }
                )
            }
            composable(
                Screen.Pedido.route,
                arguments = listOf(navArgument("folio") { type = NavType.StringType })
            ) { entrada ->
                PedidoScreen(
                    folio = entrada.arguments?.getString("folio").orEmpty(),
                    viewModel = cartViewModel,
                    onVolverInicio = {
                        navController.navigate(Screen.Inicio.route) {
                            popUpTo(Screen.Inicio.route) { inclusive = true }
                        }
                    },
                    onVerCotizaciones = { navController.popBackStack(Screen.Cart.route, inclusive = false) }
                )
            }
            composable(Screen.Rma.route) {
                RmaScreen(viewModel = rmaViewModel)
            }
        }
    }
}
