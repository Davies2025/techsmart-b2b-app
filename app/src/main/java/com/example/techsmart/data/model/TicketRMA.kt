package com.example.techsmart.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

/**
 * Colección 'rma_tickets' (compartida con la web).
 * Solicitudes de garantía y fallas prematuras (DOA: primeros 7 días).
 * `usuarioCorreo` es la cuenta propietaria; `correo` es solo de contacto.
 */
@IgnoreExtraProperties
data class TicketRMA(
    var id: String = "",
    val folio: String = "",
    val fecha: String = "",
    val estado: String = ESTADO_REPORTADO,
    val productoNombre: String = "",
    val sku: String = "",
    val numeroSerie: String = "",
    val facturaReferencia: String = "",
    val cliente: String = "",
    val contacto: String = "",
    val telefono: String = "",
    val correo: String = "",
    val descripcionFalla: String = "",

    // Sin esto, Firestore lo guardaría como "doa" por el prefijo "es"/"is" del getter
    @get:PropertyName("esDOA")
    @set:PropertyName("esDOA")
    var esDOA: Boolean = false,

    val diagnosticoTecnico: String = "",
    val resolucion: String = "",
    val usuarioCorreo: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    companion object {
        // "Reportado" | "Recibido en Bodega" | "En Diagnóstico Técnico" | "Cambio Aprobado" | "Cerrado"
        const val ESTADO_REPORTADO = "Reportado"
        const val ESTADO_CAMBIO_APROBADO = "Cambio Aprobado"

        // Mismos textos que la web
        const val DIAGNOSTICO_DOA =
            "Garantía DOA Express validada (menos de 7 días). Unidad sujeta a recambio directo en bodega."
        const val DIAGNOSTICO_NORMAL =
            "Caso ingresado. En espera de recepción física en el laboratorio técnico."
    }
}
