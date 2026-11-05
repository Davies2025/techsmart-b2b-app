package com.example.techsmart.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Mapea la colección 'tickets_rma' en Firestore.
 * Registra solicitudes de garantía y fallas prematuras (DOA < 7 días).
 */
data class TicketRMA(
    var id: String = "",
    val folio: String = "",
    val sku: String = "",
    val numeroSerie: String = "",
    val facturaReferencia: String = "",
    val cliente: String = "",
    val descripcionFalla: String = "",

    // Annotation necesaria para mapear el campo "esDOA" en Firestore sin conflictos de serialización
    @get:PropertyName("esDOA")
    @set:PropertyName("esDOA")
    var esDOA: Boolean = false,

    val estado: String = "Pendiente", // "Pendiente", "En Diagnóstico", "Resuelto"
    val diagnosticoTecnico: String = "",
    val resolucion: String = ""
)