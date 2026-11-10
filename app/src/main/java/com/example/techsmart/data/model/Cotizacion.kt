package com.example.techsmart.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

/**
 * Resumen del producto dentro de una partida.
 * La web lee `item.producto.nombre`, `item.producto.sku`, etc., así que la app
 * guarda este bloque además de los campos planos.
 */
@IgnoreExtraProperties
data class ItemProducto(
    val id: String = "",
    val nombre: String = "",
    val sku: String = "",
    val precio: Double = 0.0,
    val imagen: String = "",
    val precioVolumen: List<PrecioVolumen> = emptyList()
)

/**
 * Partida de una cotización. Congela el precio unitario con el descuento por volumen aplicado.
 *
 * - Creada en la app: trae los campos planos y el bloque `producto`.
 * - Creada en la web: solo trae `producto`, `cantidad`, `precioUnitario` y `nota`;
 *   por eso la pantalla usa los getters `*Mostrado`, que toman el valor que exista.
 */
@IgnoreExtraProperties
data class ItemCotizacion(
    val productoId: String = "",
    val sku: String = "",
    val nombre: String = "",
    val cantidad: Int = 0,
    val precioBase: Double = 0.0,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0,
    val producto: ItemProducto = ItemProducto(),
    val nota: String = ""
) {
    @get:Exclude
    val idMostrado: String get() = productoId.ifBlank { producto.id }

    @get:Exclude
    val nombreMostrado: String get() = nombre.ifBlank { producto.nombre }

    @get:Exclude
    val skuMostrado: String get() = sku.ifBlank { producto.sku }

    @get:Exclude
    val subtotalMostrado: Double
        get() = if (subtotal > 0.0) subtotal else precioUnitario * cantidad
}

/**
 * Colección 'cotizaciones' (compartida con la web).
 * Fechas como texto "dd MMM yyyy"; `usuarioCorreo` es la cuenta propietaria y `correo` es solo de contacto.
 */
@IgnoreExtraProperties
data class Cotizacion(
    var id: String = "",
    val folio: String = "",
    val fecha: String = "",
    val vencimiento: String = "",
    // Cotización: "En Revisión"
    // Compra: "Pendiente de Pago" -> "Aprobada" (pagada) -> "En Bodega" -> "Despachada" -> "Entregada"
    // Cualquiera: "Cancelada". Los avances después del pago los hace un administrador desde la web.
    val estado: String = ESTADO_EN_REVISION,
    val tipoCliente: String = TIPO_EMPRESA, // "Empresa / Corporativo" | "Cliente Particular"
    val razonSocial: String = "",
    val nit: String = "",
    val contacto: String = "",
    val telefono: String = "",
    val correo: String = "",
    val tiempoEntrega: String = "24 - 72 hrs", // "Inmediato (24h)" | "24 - 72 hrs" | "Bajo Pedido (7-15d)"
    val validezDias: Int = 7,
    val items: List<ItemCotizacion> = emptyList(),
    val subtotalNeto: Double = 0.0,
    val descuento: Double = 0.0,
    val iva: Double = 0.0, // 13% IVA (El Salvador)
    val total: Double = 0.0,
    val notas: String = "",
    val archivoAdjuntoUrl: String = "",

    // ---- Solo en compras ("Comprar ahora"); vacíos en una cotización ----
    val metodoPago: String = "",        // "tarjeta" | "transferencia" | "credito_b2b" (igual que la web)
    val pagoReferencia: String = "",    // p. ej. "Visa •••• 1111" (nunca el número completo ni el CVV)
    val documentoFiscal: String = "",   // "Comprobante de Crédito Fiscal" | "Factura Consumidor Final"

    val usuarioCorreo: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
) {
    /** true si se creó con "Comprar ahora" (tiene método de pago). */
    @get:Exclude
    val esCompra: Boolean get() = metodoPago.isNotBlank()

    companion object {
        const val TIPO_EMPRESA = "Empresa / Corporativo"
        const val TIPO_PARTICULAR = "Cliente Particular"

        val TIEMPOS_ENTREGA = listOf("Inmediato (24h)", "24 - 72 hrs", "Bajo Pedido (7-15d)")

        const val ESTADO_EN_REVISION = "En Revisión"
        const val ESTADO_PENDIENTE_PAGO = "Pendiente de Pago"
        const val ESTADO_APROBADA = "Aprobada"
        const val ESTADO_EN_BODEGA = "En Bodega"
        const val ESTADO_DESPACHADA = "Despachada"
        const val ESTADO_ENTREGADA = "Entregada"
        const val ESTADO_CANCELADA = "Cancelada"

        const val PAGO_TARJETA = "tarjeta"
        const val PAGO_TRANSFERENCIA = "transferencia"
        const val PAGO_CREDITO = "credito_b2b"

        const val DOC_CCF = "Comprobante de Crédito Fiscal"
        const val DOC_CONSUMIDOR_FINAL = "Factura Consumidor Final"
    }
}
