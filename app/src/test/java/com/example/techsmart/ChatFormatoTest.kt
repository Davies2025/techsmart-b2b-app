package com.example.techsmart

import androidx.compose.ui.text.font.FontWeight
import com.example.techsmart.ui.chat.textoConFormato
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatFormatoTest {

    @Test fun negritasSinAsteriscos() {
        val t = textoConFormato("El **SSD Kingston** cuesta **$51.20**.")
        assertEquals("El SSD Kingston cuesta $51.20.", t.text)
        val negritas = t.spanStyles.filter { it.item.fontWeight == FontWeight.Bold }
            .map { t.text.substring(it.start, it.end) }
        assertEquals(listOf("SSD Kingston", "$51.20"), negritas)
    }

    @Test fun vinetas() {
        val t = textoConFormato("Opciones:\n* Uno\n- Dos\n• Tres")
        assertEquals("Opciones:\n• Uno\n• Dos\n• Tres", t.text)
    }

    @Test fun textoSimpleQuedaIgual() {
        assertEquals("Hola", textoConFormato("Hola").text)
    }
}
