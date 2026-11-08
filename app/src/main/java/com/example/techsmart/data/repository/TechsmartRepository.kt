package com.example.techsmart.data.repository

import android.net.Uri
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.Producto
import com.example.techsmart.data.model.TicketRMA
import com.example.techsmart.data.model.Usuario
import com.example.techsmart.domain.Fechas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TechsmartRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    // ==========================================
    // SESIÓN (Firebase Authentication)
    // ==========================================
    // Correo de la cuenta con sesión iniciada, en minúsculas (igual que en la web).
    private fun correoSesion(): String? =
        auth.currentUser?.email?.trim()?.lowercase()

    private fun sinSesion(accion: String) =
        IllegalStateException("Debes iniciar sesión para $accion.")

    // Marca de tiempo de creación; los documentos sin ella quedan al final.
    private fun DocumentSnapshot.marcaDeTiempo(): Long =
        getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.toDate()?.time ?: 0L

    // ==========================================
    // 1. PRODUCTOS (Colección: productos)
    // ==========================================
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

    suspend fun getProductoById(id: String): Result<Producto?> {
        return try {
            val doc = db.collection("productos").document(id).get().await()
            val producto = doc.toObject(Producto::class.java)?.apply { this.id = doc.id }
            Result.success(producto)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. COTIZACIONES (Colección: cotizaciones)
    // ==========================================
    // Cada cotización queda a nombre de la cuenta con sesión iniciada (usuarioCorreo).
    // El campo "correo" sigue siendo el correo de contacto de la cotización.
    suspend fun guardarCotizacion(cotizacion: Cotizacion): Result<String> {
        val usuarioCorreo = correoSesion()
            ?: return Result.failure(sinSesion("guardar una cotización"))

        return try {
            val docRef = db.collection("cotizaciones").document()
            val folioGenerado = if (cotizacion.folio.isBlank()) {
                "COT-${(1000..9999).random()}" // mismo formato que la web
            } else {
                cotizacion.folio
            }

            val cotizacionFinal = cotizacion.copy(
                id = docRef.id,
                folio = folioGenerado,
                fecha = cotizacion.fecha.ifBlank { Fechas.hoy() },
                vencimiento = cotizacion.vencimiento.ifBlank {
                    Fechas.vencimientoEnDias(cotizacion.validezDias)
                },
                usuarioCorreo = usuarioCorreo,
                createdAt = null // lo llena el servidor (@ServerTimestamp)
            )
            docRef.set(cotizacionFinal).await()
            Result.success(cotizacionFinal.folio)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cotizaciones de la cuenta con sesión iniciada, de la más reciente a la más antigua
    fun getMisCotizaciones(): Flow<Result<List<Cotizacion>>> = callbackFlow {
        val usuarioCorreo = correoSesion()
        if (usuarioCorreo == null) {
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow
        }

        val listener = db.collection("cotizaciones")
            .whereEqualTo("usuarioCorreo", usuarioCorreo)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val cotizaciones = snapshot.documents
                        .sortedByDescending { it.marcaDeTiempo() }
                        .mapNotNull { doc ->
                            doc.toObject(Cotizacion::class.java)?.apply { id = doc.id }
                        }
                    trySend(Result.success(cotizaciones))
                }
            }
        awaitClose { listener.remove() }
    }

    // ==========================================
    // 3. TICKETS RMA (Colección: rma_tickets)
    // ==========================================
    suspend fun crearTicketRMA(ticket: TicketRMA): Result<String> {
        val usuarioCorreo = correoSesion()
            ?: return Result.failure(sinSesion("crear un reclamo"))

        return try {
            // Alineado con la web: colección 'rma_tickets'
            val docRef = db.collection("rma_tickets").document()
            val folioRMA = if (ticket.folio.isBlank()) {
                "RMA-2026-${(1000..9999).random()}"
            } else {
                ticket.folio
            }

            // Estado inicial igual que la web: DOA pasa directo a "Cambio Aprobado"
            val ticketFinal = ticket.copy(
                id = docRef.id,
                folio = folioRMA,
                fecha = ticket.fecha.ifBlank { Fechas.hoy() },
                estado = if (ticket.esDOA) TicketRMA.ESTADO_CAMBIO_APROBADO else TicketRMA.ESTADO_REPORTADO,
                usuarioCorreo = usuarioCorreo,
                createdAt = null // lo llena el servidor (@ServerTimestamp)
            )
            docRef.set(ticketFinal).await()
            Result.success(ticketFinal.folio)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Tickets RMA de la cuenta con sesión iniciada, del más reciente al más antiguo
    fun getMisTicketsRMA(): Flow<Result<List<TicketRMA>>> = callbackFlow {
        val usuarioCorreo = correoSesion()
        if (usuarioCorreo == null) {
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow
        }

        val listener = db.collection("rma_tickets")
            .whereEqualTo("usuarioCorreo", usuarioCorreo)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val tickets = snapshot.documents
                        .sortedByDescending { it.marcaDeTiempo() }
                        .mapNotNull { doc ->
                            doc.toObject(TicketRMA::class.java)?.apply { id = doc.id }
                        }
                    trySend(Result.success(tickets))
                }
            }
        awaitClose { listener.remove() }
    }

    // ==========================================
    // 4. PERFIL DE USUARIO (Colección: usuarios)
    // ==========================================
    // El registro y el inicio de sesión los maneja AuthRepository con Firebase Authentication.
    // Aquí solo se lee el perfil (razón social, NIT, teléfono, línea de crédito, etc.).
    suspend fun obtenerUsuarioPorCorreo(correo: String): Result<Usuario?> {
        return try {
            val correoNormalizado = correo.trim().lowercase()
            val doc = db.collection("usuarios").document(correoNormalizado).get().await()
            if (doc.exists()) {
                val user = doc.toObject(Usuario::class.java)?.apply { id = doc.id }
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Perfil de la cuenta con sesión iniciada
    suspend fun obtenerMiPerfil(): Result<Usuario?> {
        val correo = correoSesion() ?: return Result.failure(sinSesion("ver tu perfil"))
        return obtenerUsuarioPorCorreo(correo)
    }

    // ==========================================
    // 5. FIREBASE STORAGE (Subida de Adjuntos)
    // ==========================================
    suspend fun subirArchivoAdjunto(fileUri: Uri, folder: String = "rfq"): Result<String> {
        return try {
            val fileName = "${System.currentTimeMillis()}_${fileUri.lastPathSegment}"
            val fileRef = storage.reference.child("$folder/$fileName")
            fileRef.putFile(fileUri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}