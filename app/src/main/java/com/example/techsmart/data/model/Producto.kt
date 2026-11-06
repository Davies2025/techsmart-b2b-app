package com.example.techsmart.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * Representa cada especificación técnica clave-valor { label, value } de Firestore.
 */
@IgnoreExtraProperties
data class SpecItem(
    val label: String = "",
    val value: String = ""
)

/**
 * Define los tramos de precios por volumen (Tiered Pricing).
 * Incluye soporte para el campo 'price' o 'precio' y 'discountLabel' provenientes de la web.
 */
@IgnoreExtraProperties
data class PrecioVolumen(
    val minQty: Int = 0,
    @get:PropertyName("precio") @set:PropertyName("precio") var precio: Double = 0.0,
    @get:PropertyName("price") @set:PropertyName("price") var price: Double = 0.0,
    val discountLabel: String = ""
) {
    /**
     * Devuelve el precio mayorista efectivo sin importar si en Firestore se guardó como 'price' o 'precio'.
     */
    val precioEfectivo: Double
        get() = if (price > 0.0) price else precio
}

/**
 * Modelo que mapea directamente la colección 'productos' de Firestore.
 */
@IgnoreExtraProperties
data class Producto(
    var id: String = "",
    val sku: String = "",
    val nombre: String = "",
    val categoria: String = "",
    val descripcionCorta: String = "",
    val descripcionLarga: String = "",
    val precio: Double = 0.0,
    val precioVolumen: List<PrecioVolumen> = emptyList(),
    val stock: Int = 0,
    val disponibilidad: String = "inmediato",
    val bodega: String = "",
    val garantiaMeses: Int = 0,
    val specs: List<SpecItem> = emptyList(),
    val imagen: String = "",
    val destacado: Boolean = false
)