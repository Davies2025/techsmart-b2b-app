package com.example.techsmart.domain.pago

import java.util.Calendar

/**
 * Validaciones del checkout. Es un pago SIMULADO (proyecto académico):
 * solo se comprueba que el tipo y el formato de los datos sean correctos.
 * Nunca se cobra ni se guarda el número completo de la tarjeta ni el CVV.
 */

enum class MarcaTarjeta(val nombre: String, val largos: Set<Int>, val largoCvv: Int) {
    VISA("Visa", setOf(13, 16, 19), 3),
    MASTERCARD("Mastercard", setOf(16), 3),
    AMEX("American Express", setOf(15), 4)
}

object ValidacionPago {

    /** Deja solo los dígitos ("4111 1111-1111" -> "41111111111"). */
    fun soloDigitos(texto: String): String = texto.filter { it.isDigit() }

    /** Detecta la marca por los primeros dígitos (rangos IIN). */
    fun marca(numero: String): MarcaTarjeta? {
        val d = soloDigitos(numero)
        if (d.isEmpty()) return null
        val dos = d.take(2).toIntOrNull() ?: -1
        val cuatro = d.take(4).toIntOrNull() ?: -1
        return when {
            d.startsWith("4") -> MarcaTarjeta.VISA
            dos in 51..55 || (d.length >= 4 && cuatro in 2221..2720) -> MarcaTarjeta.MASTERCARD
            dos == 34 || dos == 37 -> MarcaTarjeta.AMEX
            else -> null
        }
    }

    /** Algoritmo de Luhn (dígito verificador de las tarjetas). */
    fun luhnValido(numero: String): Boolean {
        val d = soloDigitos(numero)
        if (d.length < 12) return false
        var suma = 0
        d.reversed().forEachIndexed { i, c ->
            var n = c - '0'
            if (i % 2 == 1) {
                n *= 2
                if (n > 9) n -= 9
            }
            suma += n
        }
        return suma % 10 == 0
    }

    /** Formatea en grupos para mostrar mientras se escribe (Amex 4-6-5, el resto de 4 en 4). */
    fun formatearNumero(numero: String): String {
        val d = soloDigitos(numero).take(19)
        return if (marca(d) == MarcaTarjeta.AMEX) {
            listOf(d.take(4), d.drop(4).take(6), d.drop(10).take(5)).filter { it.isNotEmpty() }.joinToString(" ")
        } else {
            d.chunked(4).joinToString(" ")
        }
    }

    fun ultimos4(numero: String): String = soloDigitos(numero).takeLast(4)

    /** Error del número de tarjeta, o null si es válido. */
    fun errorNumero(numero: String): String? {
        val d = soloDigitos(numero)
        val m = marca(d)
        return when {
            d.isEmpty() -> "Ingresa el número de la tarjeta."
            m == null -> "Solo se aceptan Visa, Mastercard o American Express."
            d.length !in m.largos -> "El número ${m.nombre} debe tener ${m.largos.sorted().joinToString(" o ")} dígitos."
            !luhnValido(d) -> "El número de tarjeta no es válido."
            else -> null
        }
    }

    /**
     * Vencimiento en formato MM/AA. La tarjeta es válida hasta el último día de ese mes.
     * @param hoy permite fijar la fecha en las pruebas.
     */
    fun errorVencimiento(texto: String, hoy: Calendar = Calendar.getInstance()): String? {
        val d = soloDigitos(texto)
        if (d.length != 4) return "Usa el formato MM/AA."
        val mes = d.take(2).toInt()
        val anio = 2000 + d.takeLast(2).toInt()
        if (mes !in 1..12) return "El mes debe estar entre 01 y 12."
        val anioActual = hoy.get(Calendar.YEAR)
        val mesActual = hoy.get(Calendar.MONTH) + 1
        if (anio < anioActual || (anio == anioActual && mes < mesActual)) return "La tarjeta está vencida."
        if (anio > anioActual + 20) return "El año de vencimiento no es válido."
        return null
    }

    /** Formatea "1127" -> "11/27" mientras se escribe. */
    fun formatearVencimiento(texto: String): String {
        val d = soloDigitos(texto).take(4)
        return if (d.length <= 2) d else d.take(2) + "/" + d.drop(2)
    }

    fun errorCvv(cvv: String, numero: String): String? {
        val d = soloDigitos(cvv)
        val largo = marca(numero)?.largoCvv ?: 3
        return when {
            d.length != cvv.length || d.isEmpty() -> "El CVV solo lleva números."
            d.length != largo -> "El CVV debe tener $largo dígitos."
            else -> null
        }
    }

    fun errorTitular(nombre: String): String? {
        val limpio = nombre.trim()
        return when {
            limpio.length < 3 -> "Escribe el nombre como aparece en la tarjeta."
            !limpio.all { it.isLetter() || it == ' ' || it == '.' || it == '\'' || it == '-' } ->
                "El nombre solo puede llevar letras."
            else -> null
        }
    }

    // ---------------------------------------------------------------------
    // Documentos de El Salvador
    // ---------------------------------------------------------------------

    /** DUI: 8 dígitos, guion y dígito verificador (########-#). */
    fun duiValido(dui: String): Boolean = Regex("""^\d{8}-\d$""").matches(dui.trim())

    /** NIT: ####-######-###-# (14 dígitos). */
    fun nitValido(nit: String): Boolean = Regex("""^\d{4}-\d{6}-\d{3}-\d$""").matches(nit.trim())

    /**
     * Documento para la factura:
     * - Empresa: NIT (para Crédito Fiscal, CCF).
     * - Persona: DUI, o "Consumidor Final" si no lo da.
     */
    fun errorDocumentoFiscal(documento: String, esEmpresa: Boolean): String? {
        val d = documento.trim()
        return if (esEmpresa) {
            if (nitValido(d)) null else "El NIT debe tener el formato 0614-010190-101-1."
        } else {
            if (d.isEmpty() || d.equals("Consumidor Final", ignoreCase = true) || duiValido(d)) null
            else "El DUI debe tener el formato 12345678-9 (o déjalo vacío para Consumidor Final)."
        }
    }

    /** Línea de crédito: el total no puede superar el saldo disponible. */
    fun errorCredito(total: Double, lineaDisponible: Double): String? = when {
        lineaDisponible <= 0.0 -> "Tu cuenta no tiene línea de crédito disponible."
        total > lineaDisponible -> "El total supera tu línea de crédito disponible."
        else -> null
    }
}
