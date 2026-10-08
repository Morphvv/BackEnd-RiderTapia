package cl.ridertapia.reportesservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * Lo que reportes-service necesita de un pedido del servicio de pedidos.
 * Se ignoran los demás campos (cupón, empleado, datos del visitante, enlaces HATEOAS, etc.).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PedidoDto(
        Long idPedido,
        String numero,
        LocalDateTime fechaCreacion,
        String estado,
        String canal,
        Integer subtotal,
        Integer descuento,
        Integer total,
        String rutCliente
) {
}
