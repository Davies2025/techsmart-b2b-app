package com.example.techsmart.data.repository

import android.net.Uri
import com.example.techsmart.data.model.Cotizacion
import com.example.techsmart.data.model.Producto
import com.example.techsmart.data.model.TicketRMA
import com.example.techsmart.data.model.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TechsmartRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    // ==========================================
    // SEGURIDAD: HASHING DE CONTRASEÑA (SHA-256)
    // ==========================================
    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. COTIZACIONES (Colección: cotizaciones)
    // ==========================================
    suspend fun guardarCotizacion(cotizacion: Cotizacion): Result<String> {
        return try {
            val docRef = db.collection("cotizaciones").document()
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

    // ==========================================
    // 3. TICKETS RMA (Colección: rma_tickets)
    // ==========================================
    suspend fun crearTicketRMA(ticket: TicketRMA): Result<String> {
        return try {
            // Alineado con la web: colección 'rma_tickets'
            val docRef = db.collection("rma_tickets").document()
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

    // ==========================================
    // 4. USUARIOS Y AUTENTICACIÓN (Colección: usuarios)
    // ==========================================
    suspend fun registrarUsuario(usuario: Usuario, rawPassword: String): Result<Usuario> {
        return try {
            val correoNormalizado = usuario.correo.trim().lowercase()
            val docRef = db.collection("usuarios").document(correoNormalizado)

            val passwordHash = if (rawPassword.isNotBlank()) hashPassword(rawPassword) else ""
            val fechaActual = SimpleDateFormat("dd MMM yyyy", Locale("es", "SV")).format(Date())

            val usuarioFinal = usuario.copy(
                id = correoNormalizado,
                correo = correoNormalizado,
                passwordHash = passwordHash,
                rol = "cliente_b2b",
                estado = "activo",
                fechaRegistro = fechaActual
            )

            docRef.set(usuarioFinal).await()
            Result.success(usuarioFinal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
        } catch (e: Exception) {
            Result.failure(e)
        }
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}