package com.example.techsmart

import com.example.techsmart.data.model.PrecioVolumen
import com.example.techsmart.data.model.Producto
import com.example.techsmart.domain.engine.TieredPricingEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class TotalesTest {

    private val accessPoint = Producto(
        id = "ap", sku = "NET-UBIQ-U6-PRO", nombre = "Access Point", precio = 189.50,
        precioVolumen = listOf(PrecioVolumen(minQty = 3, price = 179.00), PrecioVolumen(minQty = 10, price = 169.00))
    )

    @Test fun centavosRedondeaMitadHaciaArriba() {
        assertEquals(214.14, TieredPricingEngine.centavos(214.135), 0.0)
        assertEquals(69.81, TieredPricingEngine.centavos(69.81), 0.0)
        assertEquals(0.01, TieredPricingEngine.centavos(0.005), 0.0)
    }

    @Test fun totalesQuedanEnCentavos() {
        val t = TieredPricingEngine.calcularTotales(listOf(TieredPricingEngine.procesarPartida(accessPoint, 1)))
        assertEquals(189.50, t.subtotalNeto, 0.0)
        assertEquals(24.64, t.iva, 0.0)   // 189.50 × 0.13 = 24.635
        assertEquals(214.14, t.total, 0.0)
    }

    @Test fun dosComprasSumanLoQueSeMuestra() {
        val t = TieredPricingEngine.calcularTotales(listOf(TieredPricingEngine.procesarPartida(accessPoint, 1)))
        assertEquals(428.28, TieredPricingEngine.centavos(t.total + t.total), 0.0)
    }

    @Test fun precioPorVolumenYDescuento() {
        val t = TieredPricingEngine.calcularTotales(listOf(TieredPricingEngine.procesarPartida(accessPoint, 3)))
        assertEquals(537.00, t.subtotalNeto, 0.0)
        assertEquals(31.50, t.descuento, 0.0)
        assertEquals(69.81, t.iva, 0.0)
        assertEquals(606.81, t.total, 0.0)
    }
}
