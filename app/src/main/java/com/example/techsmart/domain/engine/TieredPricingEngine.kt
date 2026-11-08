package com.example.techsmart.domain.engine

import com.example.techsmart.data.model.ItemCotizacion
import com.example.techsmart.data.model.ItemProducto
import com.example.techsmart.data.model.Producto

/**
 * Motor de Cálculo Comercial y Precios Dinámicos por Escala (Tiered Pricing Engine).
 * Aplica la regla: P_unitario = precio del tramo con mayor minQty tal que Cantidad >= minQty
 * e integra la normativa fiscal de El Salvador (13% IVA).
 * Es la misma lógica que usa la web en CartContext.
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
     * Incluye el bloque `producto` para que la web pueda mostrar la partida.
     */
    fun procesarPartida(producto: Producto, cantidad: Int): ItemCotizacion {
        val precioUnitario = calcularPrecioUnitario(producto, cantidad)

        return ItemCotizacion(
            productoId = producto.id,
            sku = producto.sku,
            nombre = producto.nombre,
            cantidad = cantidad,
            precioBase = producto.precio,
            precioUnitario = precioUnitario,
            subtotal = precioUnitario * cantidad,
            producto = ItemProducto(
                id = producto.id,
                nombre = producto.nombre,
                sku = producto.sku,
                precio = producto.precio,
                imagen = producto.imagen
            )
        )
    }

    /**
     * Cómputo global del carrito / proforma (igual que la web):
     * descuento = (precio base × cantidad) − subtotal neto; IVA 13% sobre el subtotal neto.
     */
    fun calcularTotales(items: List<ItemCotizacion>): TotalesCotizacion {
        val subtotalBase = items.sumOf { it.precioBase * it.cantidad }
        val subtotalNeto = items.sumOf { it.subtotalMostrado }
        val descuento = (subtotalBase - subtotalNeto).coerceAtLeast(0.0)
        val iva = subtotalNeto * IVA_EL_SALVADOR
        val total = subtotalNeto + iva

        return TotalesCotizacion(
            subtotalNeto = subtotalNeto,
            descuento = descuento,
            iva = iva,
            total = total
        )
    }
}

/**
 * Objeto de transferencia para desgloses financieros de la cotización.
 */
data class TotalesCotizacion(
    val subtotalNeto: Double = 0.0,
    val descuento: Double = 0.0,
    val iva: Double = 0.0,
    val total: Double = 0.0
)
