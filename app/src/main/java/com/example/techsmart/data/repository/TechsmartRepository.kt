package com.example.techsmart.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.Producto
import com.example.techsmart.data.model.TicketRMA
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TechsmartRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // ==========================================
    // 1. CATÁLOGO DE PRODUCTOS (Colección: productos)
    // ==========================================

    /**
     * Escucha el catálogo de productos en tiempo real mediante un Flow.
     * Mapea la colección 'productos' de Firestore a la lista de objetos Producto.
     */
    fun getProductosStream(): Flow<Result<List<Producto>>> = callbackFlow {
        val listener = db.collection("productos")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val productos = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Producto::class.java)?.apply { id = doc.id }
                    }
                    trySend(Result.success(productos))
                }
            }
        awaitClose { listener.remove() }
    }

    /**
     * Consulta puntual de un producto por su ID semántico o document ID.
     */
    suspend fun getProductoById(id: String): Result<Producto?> {
        return try {
            val doc = db.collection("productos").document(id).get().await()
            val producto = doc.toObject(Producto::class.java)?.apply { this.id = doc.id }
            Result.success(producto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===========================
    // 2. MÓDULO DE COTIZACIONES
    // ===========================

    /**
     * Registra una nueva cotización formal con su folio fiscal y desglose de partidas.
     */
    suspend fun guardarCotizacion(cotizacion: Cotizacion): Result<String> {
        return try {
            val docRef = db.collection("cotizaciones").document()

            // Generación de folio alfanumérico si no viene definido
            val folioGenerado = if (cotizacion.folio.isBlank()) {
                "COT-${(1000..9999).random()}/B2B-${(100..999).random()}"
            } else {
                cotizacion.folio
            }

            val cotizacionFinal = cotizacion.copy(id = docRef.id, folio = folioGenerado)
            docRef.set(cotizacionFinal).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene el historial de cotizaciones de un cliente ordenadas por fecha descendente.
     */
    fun getCotizacionesByCorreo(correo: String): Flow<Result<List<Cotizacion>>> = callbackFlow {
        val listener = db.collection("cotizaciones")
            .whereEqualTo("correo", correo)
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val cotizaciones = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Cotizacion::class.java)?.apply { id = doc.id }
                    }
                    trySend(Result.success(cotizaciones))
                }
            }
        awaitClose { listener.remove() }
    }

    // ==============================
    // 3. MÓDULO DE GARANTÍAS Y RMA
    // ==============================

    /**
     * Registra un ticket de reclamo RMA o falla prematura DOA (< 7 días).
     */
    suspend fun crearTicketRMA(ticket: TicketRMA): Result<String> {
        return try {
            val docRef = db.collection("tickets_rma").document()

            val folioRMA = if (ticket.folio.isBlank()) {
                "RMA-2026-${(1000..9999).random()}"
            } else {
                ticket.folio
            }

            val ticketFinal = ticket.copy(id = docRef.id, folio = folioRMA)
            docRef.set(ticketFinal).await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene los tickets de reclamo asociados a un cliente.
     */
    fun getTicketsRMAByCliente(cliente: String): Flow<Result<List<TicketRMA>>> = callbackFlow {
        val listener = db.collection("tickets_rma")
            .whereEqualTo("cliente", cliente)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tickets = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(TicketRMA::class.java)?.apply { id = doc.id }
                    }
                    trySend(Result.success(tickets))
                }
            }
        awaitClose { listener.remove() }
    }
}