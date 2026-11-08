package com.example.techsmart.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Perfil del cliente en 'usuarios/{correo en minúsculas}' (mismo esquema que la web).
 * La contraseña la gestiona Firebase Authentication: aquí no se guarda.
 * En cuentas personales `razonSocial` = nombre y `nit` = DUI o "Consumidor Final".
 */
@IgnoreExtraProperties
data class Usuario(
    var id: String = "",
    val tipoCuenta: String = "personal", // "empresarial" | "personal"
    val razonSocial: String = "",
    val nit: String = "",
    val nombreContacto: String = "",
    val telefono: String = "",
    val correo: String = "",
    val rol: String = "cliente_b2b",
    val lineaCredito: Long = 0, // entero: 25000 empresarial / 3500 personal
    val estado: String = "activo",
    val fechaRegistro: String = "",
    val origen: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    @get:Exclude
    val esEmpresa: Boolean get() = tipoCuenta == "empresarial"
}
