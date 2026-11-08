package com.example.techsmart.domain

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fechas en texto con el mismo formato que la web: "07 nov 2026".
 * La web usa toLocaleDateString("es-SV", { day: "2-digit", month: "short", year: "numeric" }),
 * que no lleva punto en el mes; Java a veces escribe "nov.", por eso se quita.
 */
object Fechas {
    private val LOCALE_SV: Locale = Locale.forLanguageTag("es-SV")
    private const val DIA_MS = 24L * 60 * 60 * 1000

    fun formatear(fecha: Date): String =
        SimpleDateFormat("dd MMM yyyy", LOCALE_SV).format(fecha).replace(".", "")

    fun hoy(): String = formatear(Date())

    /** Igual que la web: "14 nov 2026 (en 7 días)". */
    fun vencimientoEnDias(dias: Int): String {
        val fecha = Date(System.currentTimeMillis() + dias * DIA_MS)
        return "${formatear(fecha)} (en $dias días)"
    }
}
