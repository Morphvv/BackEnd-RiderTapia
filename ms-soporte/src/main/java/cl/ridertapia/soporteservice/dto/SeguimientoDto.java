package cl.ridertapia.soporteservice.dto;

import cl.ridertapia.soporteservice.model.Seguimiento;
import cl.ridertapia.soporteservice.util.RutUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.hateoas.server.core.Relation;
import java.time.LocalDateTime;

public final class SeguimientoDto {

    private SeguimientoDto() {
    }

    @Schema(description = "Datos para programar un seguimiento posventa")
    public record Crear(
            @NotNull(message = "El id del pedido es obligatorio")
            @Schema(example = "1")
            Long idPedido,

            @NotBlank(message = "El RUT es obligatorio")
            @Pattern(regexp = RutUtil.RUT_REGEX, message = "El RUT debe tener formato 12345678-9")
            @Schema(example = "12345678-5")
            String rutUsuario,

            @NotNull(message = "El tipo es obligatorio (LLAMADA, CORREO o ENCUESTA)")
            Seguimiento.Tipo tipo,

            @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
            @Schema(example = "Llamar en horario de la tarde")
            String observacion
    ) {
    }

    public record ActualizarEstado(
            @NotNull(message = "El estado es obligatorio (PENDIENTE, REALIZADO o CANCELADO)")
            Seguimiento.Estado estado,

            @Size(max = 500, message = "La observación no puede superar los 500 caracteres")
            String observacion
    ) {
    }

    @Relation(collectionRelation = "seguimientos", itemRelation = "seguimiento")
    @Schema(description = "Seguimiento posventa de un pedido")
    public record Respuesta(
            Long idSeguimiento,
            Long idPedido,
            String rutUsuario,
            Seguimiento.Tipo tipo,
            Seguimiento.Estado estado,
            String observacion,
            LocalDateTime fecha
    ) {
        public static Respuesta desde(Seguimiento s) {
            return new Respuesta(s.getIdSeguimiento(), s.getIdPedido(), s.getRutUsuario(),
                    s.getTipo(), s.getEstado(), s.getObservacion(), s.getFecha());
        }
    }
}
