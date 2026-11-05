package com.example.techsmart.data.model

/**
 * Define los tramos de precios por volumen (Tiered Pricing).
 * Representa la matriz de volumen asociada a cada producto.
 */
data class PrecioVolumen(
    val minQty: Int = 0,
    val precio: Double = 0.0
)

/**
 * Modelo que mapea directamente la colección 'productos' de Firestore.
 */
data class Producto(
    var id: String = "",
    val sku: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val precio: Double = 0.0,
    val precioVolumen: List<PrecioVolumen> = emptyList(),
    val stock: Int = 0,
    val bodega: String = "",
    val garantiaMeses: Int = 0,
    val specs: List<String> = emptyList(),
    val imagen: String = "",
    val destacado: Boolean = false
)