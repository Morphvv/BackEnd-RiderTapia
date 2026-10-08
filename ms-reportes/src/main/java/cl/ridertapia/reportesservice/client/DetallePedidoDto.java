package cl.ridertapia.reportesservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Línea de un pedido. El servicio de pedidos incluye el pedido completo dentro de cada detalle. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DetallePedidoDto(
        Long idDetallePedido,
        Long idVariante,
        Integer cantidad,
        Integer precioUnitario,
        Integer subtotal,
        PedidoDto pedido
) {
}
