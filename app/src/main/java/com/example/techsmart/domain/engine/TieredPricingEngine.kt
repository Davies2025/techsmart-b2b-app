package com.example.techsmart.domain.engine

import com.example.techsmart.data.model.ItemCotizacion
import com.example.techsmart.data.model.Producto

/**
 * Motor de Cálculo Comercial y Precios Dinámicos por Escala (Tiered Pricing Engine).
 * Aplica la regla: P_unitario = max(tier.price | Cantidad >= tier.minQty)
 * e integra la normativa fiscal de El Salvador (13% IVA).
 */
object TieredPricingEngine {

    const val IVA_EL_SALVADOR = 0.13

    /**
     * Evalúa la matriz de descuentos escalonados asociada al producto.
     * Utiliza 'precioEfectivo' para soportar tanto 'price' (Web) como 'precio'.
     */
    fun calcularPrecioUnitario(producto: Producto, cantidad: Int): Double {
        if (cantidad <= 0) return producto.precio

        val tierAplicable = producto.precioVolumen
            .filter { cantidad >= it.minQty }
            .maxByOrNull { it.minQty }

        val precioTier = tierAplicable?.precioEfectivo ?: 0.0

        // Si el tramo escalonado tiene un valor mayor a cero lo utiliza; de lo contrario usa el precio base
        return if (precioTier > 0.0) precioTier else producto.precio
    }

    /**
     * Construye una partida formal (ItemCotizacion) aplicando el precio escalonado correspondiente.
     */
    fun procesarPartida(producto: Producto, cantidad: Int): ItemCotizacion {
        val precioUnitario = calcularPrecioUnitario(producto, cantidad)
        val subtotal = precioUnitario * cantidad

        return ItemCotizacion(
            productoId = producto.id,
            sku = producto.sku,
            nombre = producto.nombre,
            cantidad = cantidad,
            precioUnitario = precioUnitario,
            subtotal = subtotal
        )
    }

    /**
     * Cómputo global del carrito / proforma:
     * Calcula Subtotal Neto, desglosa el 13% de IVA oficial y retorna el Total final.
     */
    fun calcularTotales(items: List<ItemCotizacion>): TotalesCotizacion {
        val subtotalNeto = items.sumOf { it.subtotal }
        val iva = subtotalNeto * IVA_EL_SALVADOR
        val total = subtotalNeto + iva

        return TotalesCotizacion(
            subtotalNeto = subtotalNeto,
            iva = iva,
            total = total
        )
    }
}

/**
 * Objeto de transferencia para desgloses financieros de la cotización.
 */
data class TotalesCotizacion(
    val subtotalNeto: Double,
    val iva: Double,
    val total: Double
)