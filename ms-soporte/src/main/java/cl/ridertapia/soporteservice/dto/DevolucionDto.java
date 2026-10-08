package cl.ridertapia.soporteservice.dto;

import cl.ridertapia.soporteservice.model.Devolucion;
import cl.ridertapia.soporteservice.util.RutUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.hateoas.server.core.Relation;
import java.time.LocalDateTime;

public final class DevolucionDto {

    private DevolucionDto() {
    }

    @Schema(description = "Datos para solicitar una devolución")
    public record Crear(
            @NotBlank(message = "El RUT es obligatorio")
            @Pattern(regexp = RutUtil.RUT_REGEX, message = "El RUT debe tener formato 12345678-9")
            @Schema(example = "12345678-5")
            String rutUsuario,

            @NotNull(message = "El id del pedido es obligatorio")
            @Schema(example = "1")
            Long idPedido,

            @NotNull(message = "El id del producto es obligatorio")
            @Schema(example = "100")
            Long idProducto,

            @NotBlank(message = "El motivo es obligatorio")
            @Size(min = 10, max = 500, message = "El motivo debe tener entre 10 y 500 caracteres")
            @Schema(example = "El producto llegó con la llanta dañada")
            String motivo
    ) {
    }

    /** Resolución del administrador: aprobar, rechazar o marcar como completada. */
    public record Resolver(
            @NotNull(message = "El estado es obligatorio (APROBADA, RECHAZADA o COMPLETADA)")
            Devolucion.Estado estado,

            @Size(max = 500, message = "La respuesta no puede superar los 500 caracteres")
            String respuesta
    ) {
    }

    @Relation(collectionRelation = "devoluciones", itemRelation = "devolucion")
    @Schema(description = "Solicitud de devolución")
    public record Respuesta(
            Long idDevolucion,
            String rutUsuario,
            Long idPedido,
            Long idProducto,
            String motivo,
            Devolucion.Estado estado,
            LocalDateTime fechaSolicitud,
            LocalDateTime fechaResolucion,
            String respuesta
    ) {
        public static Respuesta desde(Devolucion d) {
            return new Respuesta(d.getIdDevolucion(), d.getRutUsuario(), d.getIdPedido(), d.getIdProducto(),
                    d.getMotivo(), d.getEstado(), d.getFechaSolicitud(), d.getFechaResolucion(), d.getRespuesta());
        }
    }
}
