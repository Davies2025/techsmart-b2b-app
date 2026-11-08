package com.example.techsmart

import com.example.techsmart.domain.pago.MarcaTarjeta
import com.example.techsmart.domain.pago.ValidacionPago
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ValidacionPagoTest {

    // Números de prueba publicados por las marcas (pasan Luhn, no son tarjetas reales)
    private val visa = "4111 1111 1111 1111"
    private val mastercard = "5555 5555 5555 4444"
    private val mastercard2 = "2223 0031 2200 3222"
    private val amex = "3782 822463 10005"

    private fun fecha(anio: Int, mes: Int) = Calendar.getInstance().apply {
        clear(); set(anio, mes - 1, 15)
    }

    @Test fun detectaMarcas() {
        assertEquals(MarcaTarjeta.VISA, ValidacionPago.marca(visa))
        assertEquals(MarcaTarjeta.MASTERCARD, ValidacionPago.marca(mastercard))
        assertEquals(MarcaTarjeta.MASTERCARD, ValidacionPago.marca(mastercard2))
        assertEquals(MarcaTarjeta.AMEX, ValidacionPago.marca(amex))
        assertNull(ValidacionPago.marca("6011 1111 1111 1117")) // Discover: no aceptada
    }

    @Test fun luhn() {
        assertTrue(ValidacionPago.luhnValido(visa))
        assertTrue(ValidacionPago.luhnValido(amex))
        assertFalse(ValidacionPago.luhnValido("4111 1111 1111 1112"))
    }

    @Test fun errorNumero() {
        assertNull(ValidacionPago.errorNumero(visa))
        assertNull(ValidacionPago.errorNumero(amex))
        assertNotNull(ValidacionPago.errorNumero(""))
        assertNotNull(ValidacionPago.errorNumero("4111 1111 1111"))       // largo incorrecto
        assertNotNull(ValidacionPago.errorNumero("4111 1111 1111 1112"))  // Luhn
        assertNotNull(ValidacionPago.errorNumero("6011 1111 1111 1117"))  // marca no aceptada
    }

    @Test fun formatoNumero() {
        assertEquals("4111 1111 1111 1111", ValidacionPago.formatearNumero("4111111111111111"))
        assertEquals("3782 822463 10005", ValidacionPago.formatearNumero("378282246310005"))
        assertEquals("1111", ValidacionPago.ultimos4(visa))
    }

    @Test fun vencimiento() {
        val hoy = fecha(2026, 11)
        assertNull(ValidacionPago.errorVencimiento("11/26", hoy))   // vence a fin de este mes
        assertNull(ValidacionPago.errorVencimiento("0130", hoy))
        assertNotNull(ValidacionPago.errorVencimiento("10/26", hoy)) // vencida
        assertNotNull(ValidacionPago.errorVencimiento("13/27", hoy)) // mes inválido
        assertNotNull(ValidacionPago.errorVencimiento("1/27", hoy))  // formato
        assertEquals("11/27", ValidacionPago.formatearVencimiento("1127"))
    }

    @Test fun cvv() {
        assertNull(ValidacionPago.errorCvv("123", visa))
        assertNotNull(ValidacionPago.errorCvv("1234", visa))
        assertNull(ValidacionPago.errorCvv("1234", amex))
        assertNotNull(ValidacionPago.errorCvv("12a", visa))
    }

    @Test fun titular() {
        assertNull(ValidacionPago.errorTitular("María José Pérez"))
        assertNotNull(ValidacionPago.errorTitular("Jo"))
        assertNotNull(ValidacionPago.errorTitular("Juan 123"))
    }

    @Test fun documentosElSalvador() {
        assertTrue(ValidacionPago.duiValido("12345678-9"))
        assertFalse(ValidacionPago.duiValido("123456789"))
        assertTrue(ValidacionPago.nitValido("0614-010190-101-1"))
        assertFalse(ValidacionPago.nitValido("0614010190101"))

        assertNull(ValidacionPago.errorDocumentoFiscal("0614-010190-101-1", esEmpresa = true))
        assertNotNull(ValidacionPago.errorDocumentoFiscal("12345678-9", esEmpresa = true))
        assertNull(ValidacionPago.errorDocumentoFiscal("12345678-9", esEmpresa = false))
        assertNull(ValidacionPago.errorDocumentoFiscal("Consumidor Final", esEmpresa = false))
        assertNull(ValidacionPago.errorDocumentoFiscal("", esEmpresa = false))
        assertNotNull(ValidacionPago.errorDocumentoFiscal("1234", esEmpresa = false))
    }

    @Test fun credito() {
        assertNull(ValidacionPago.errorCredito(1000.0, 3500.0))
        assertNotNull(ValidacionPago.errorCredito(4000.0, 3500.0))
        assertNotNull(ValidacionPago.errorCredito(10.0, 0.0))
    }
}
