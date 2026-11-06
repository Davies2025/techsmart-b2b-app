package com.example.techsmart.data.model

data class Usuario(
    var id: String = "",
    val nombre: String = "",
    val empresa: String = "",
    val correo: String = "",
    val telefono: String = "",
    val nit: String = "",
    val passwordHash: String = "",
    val rol: String = "cliente_b2b",
    val estado: String = "activo",
    val fechaRegistro: String = ""
)