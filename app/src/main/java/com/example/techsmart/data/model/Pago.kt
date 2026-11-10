package com.example.techsmart.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Colección 'pagos': un registro por cada pago de una compra ("Comprar ahora").
 * Se guarda junto con el pedido (colección 'cotizaciones') en una sola operación.
 *
 * El pago es SIMULADO (proyecto académico): solo se guarda una referencia enmascarada
 * ("Visa •••• 1111"); nunca el número completo de la tarjeta ni el CVV.
 */
@IgnoreExtraProperties
data class Pago(
    var id: String = "",
    val pedidoId: String = "",        // id del documento en 'cotizaciones'
    val folioPedido: String = "",     // "B2B-####"
    val usuarioCorreo: String = "",   // cuenta que pagó (la asigna el repositorio)
    val metodo: String = "",          // Cotizacion.PAGO_* : "tarjeta" | "transferencia" | "credito_b2b"
    val referencia: String = "",      // "Visa •••• 1111", "Transferencia bancaria", "Crédito Techsmart 30 días"
    val monto: Double = 0.0,          // total con IVA
    val iva: Double = 0.0,
    val moneda: String = "USD",
    val estado: String = ESTADO_APROBADO, // "Aprobado" | "Pendiente" (transferencia por confirmar)
    val fecha: String = "",           // "09 nov 2026"
    val hora: String = "",            // "05:42 PM"
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    companion object {
        const val ESTADO_APROBADO = "Aprobado"
        const val ESTADO_PENDIENTE = "Pendiente"
    }
}
