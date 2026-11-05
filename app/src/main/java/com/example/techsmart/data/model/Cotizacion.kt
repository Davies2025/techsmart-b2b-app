package com.example.techsmart.data.model

/**
 * Representa una partida dentro de la cotización formal.
 * Congela el precio unitario calculado con el descuento de volumen aplicado.
 */
data class ItemCotizacion(
    val productoId: String = "",
    val sku: String = "",
    val nombre: String = "",
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0
)

/**
 * Mapea la colección 'cotizaciones' en Firestore.
 * Incluye los datos fiscales del cliente y el desglose oficial del 13% de IVA.
 */
data class Cotizacion(
    var id: String = "",
    val folio: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val vencimiento: Long = 0,
    val estado: String = "En Revisión", // "En Revisión", "Aprobada", "Despachada"
    val razonSocial: String = "",
    val nit: String = "",
    val contacto: String = "",
    val correo: String = "",
    val items: List<ItemCotizacion> = emptyList(),
    val subtotalNeto: Double = 0.0,
    val descuento: Double = 0.0,
    val iva: Double = 0.0, // 13% IVA (El Salvador)
    val total: Double = 0.0
)