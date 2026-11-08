package com.example.techsmart.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import com.example.techsmart.domain.Fechas
import kotlinx.coroutines.tasks.await

// Línea de crédito inicial según tipo de cuenta.
// Deben coincidir con la web (LINEA_CREDITO_INICIAL) y con las reglas de Firestore.
private const val CUPO_EMPRESARIAL = 25000
private const val CUPO_PERSONAL = 3500

data class RegistroDatos(
    val tipoCuenta: String,          // "empresarial" | "personal"
    val razonSocial: String = "",    // solo empresarial
    val nit: String = "",            // solo empresarial
    val nombreContacto: String,
    val telefono: String,
    val correo: String,
    val dui: String = ""             // solo personal (opcional)
)

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val isLoggedIn: Boolean get() = auth.currentUser != null
    val currentEmail: String? get() = auth.currentUser?.email

    suspend fun register(datos: RegistroDatos, password: String): Result<Unit> = resultado {
        val correo = datos.correo.trim().lowercase()
        val user = auth.createUserWithEmailAndPassword(correo, password).await().user
            ?: error("No se pudo crear el usuario")
        user.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(datos.nombreContacto).build()
        ).await()
        guardarPerfil(datos.copy(correo = correo))
    }

    suspend fun login(correoIngresado: String, password: String): Result<Unit> = resultado {
        val correo = correoIngresado.trim().lowercase()
        val user = auth.signInWithEmailAndPassword(correo, password).await().user
            ?: error("No se pudo iniciar sesión")

        // Si la cuenta existe en Auth pero no tiene perfil en 'usuarios'
        // (por ejemplo, falló el guardado al registrarse), se crea uno básico.
        val existe = db.collection("usuarios").document(correo).get().await().exists()
        if (!existe) {
            guardarPerfil(
                RegistroDatos(
                    tipoCuenta = "personal",
                    nombreContacto = user.displayName.orEmpty(),
                    telefono = "",
                    correo = correo
                )
            )
        }
    }

    suspend fun resetPassword(correo: String): Result<Unit> = resultado {
        auth.sendPasswordResetEmail(correo.trim().lowercase()).await()
    }

    fun logout() = auth.signOut()

    private suspend fun guardarPerfil(d: RegistroDatos) {
        val esEmpresa = d.tipoCuenta == "empresarial"
        val fecha = Fechas.hoy()

        val perfil = mapOf<String, Any>(
            "tipoCuenta" to d.tipoCuenta,
            "razonSocial" to if (esEmpresa) d.razonSocial else d.nombreContacto,
            "nit" to if (esEmpresa) d.nit else d.dui.ifBlank { "Consumidor Final" },
            "nombreContacto" to d.nombreContacto,
            "telefono" to d.telefono,
            "correo" to d.correo,
            "rol" to "cliente_b2b",
            "lineaCredito" to if (esEmpresa) CUPO_EMPRESARIAL else CUPO_PERSONAL, // entero, no decimal
            "estado" to "activo",
            "fechaRegistro" to fecha,
            "origen" to "app",
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("usuarios").document(d.correo).set(perfil).await()
    }

    // Como runCatching, pero deja pasar la cancelación de corrutinas
    private suspend fun resultado(bloque: suspend () -> Unit): Result<Unit> =
        try {
            bloque()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}

fun Throwable.toFriendlyMessage(): String = when (this) {
    is FirebaseAuthWeakPasswordException -> "La contraseña debe tener al menos 6 caracteres."
    is FirebaseAuthUserCollisionException -> "Este correo ya está registrado."
    is FirebaseAuthInvalidCredentialsException,
    is FirebaseAuthInvalidUserException -> "Correo o contraseña incorrectos."
    is FirebaseNetworkException -> "Sin conexión a internet."
    is FirebaseFirestoreException ->
        if (code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
            "No tienes permiso para completar esta acción."
        else "Ocurrió un error al guardar tus datos."
    // Errores propios del repositorio (p. ej. "Debes iniciar sesión para ...")
    is IllegalStateException -> message ?: "Ocurrió un error. Inténtalo de nuevo."
    else -> "Ocurrió un error. Inténtalo de nuevo."
}