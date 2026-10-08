package cl.ridertapia.soporteservice.dto;

import cl.ridertapia.soporteservice.model.Resena;
import cl.ridertapia.soporteservice.util.RutUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.hateoas.server.core.Relation;
import java.time.LocalDateTime;

public final class ResenaDto {

    private ResenaDto() {
    }

    @Schema(description = "Datos para publicar una reseña")
    public record Crear(
            @NotBlank(message = "El RUT es obligatorio")
            @Pattern(regexp = RutUtil.RUT_REGEX, message = "El RUT debe tener formato 12345678-9")
            @Schema(example = "12345678-5")
            String rutUsuario,

            @NotNull(message = "El id del producto es obligatorio")
            @Schema(example = "100")
            Long idProducto,

            @NotNull(message = "La calificación es obligatoria")
            @Min(value = 1, message = "La calificación mínima es 1")
            @Max(value = 5, message = "La calificación máxima es 5")
            @Schema(example = "5")
            Integer calificacion,

            @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
            @Schema(example = "Excelente bicicleta")
            String comentario
    ) {
    }

    @Relation(collectionRelation = "resenas", itemRelation = "resena")
    @Schema(description = "Reseña de un producto")
    public record Respuesta(
            Long idResena,
            String rutUsuario,
            Long idProducto,
            Integer calificacion,
            String comentario,
            LocalDateTime fecha,
            Resena.Estado estado
    ) {
        public static Respuesta desde(Resena r) {
            return new Respuesta(r.getIdResena(), r.getRutUsuario(), r.getIdProducto(),
                    r.getCalificacion(), r.getComentario(), r.getFecha(), r.getEstado());
        }
    }

    /** Promedio de calificaciones visibles de un producto. */
    public record Resumen(Long idProducto, double promedio, long totalResenas) {
    }
}
