package cl.ridertapia.soporteservice.dto;

import cl.ridertapia.soporteservice.model.Garantia;
import cl.ridertapia.soporteservice.util.RutUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.hateoas.server.core.Relation;
import java.time.LocalDate;

public final class GarantiaDto {

    private GarantiaDto() {
    }

    @Schema(description = "Datos para registrar una garantía")
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

            // Opcional: si no se envía, se usan 12 meses
            @Min(value = 1, message = "La cobertura mínima es 1 mes")
            @Max(value = 60, message = "La cobertura máxima es 60 meses")
            @Schema(example = "12")
            Integer mesesCobertura
    ) {
    }

    @Relation(collectionRelation = "garantias", itemRelation = "garantia")
    @Schema(description = "Garantía de un producto comprado")
    public record Respuesta(
            Long idGarantia,
            String rutUsuario,
            Long idPedido,
            Long idProducto,
            LocalDate fechaInicio,
            LocalDate fechaTermino,
            Garantia.Estado estado
    ) {
        public static Respuesta desde(Garantia g) {
            return new Respuesta(g.getIdGarantia(), g.getRutUsuario(), g.getIdPedido(),
                    g.getIdProducto(), g.getFechaInicio(), g.getFechaTermino(), g.getEstado());
        }
    }

    public record Vigencia(Long idGarantia, boolean vigente, LocalDate fechaTermino, long diasRestantes) {
    }
}
