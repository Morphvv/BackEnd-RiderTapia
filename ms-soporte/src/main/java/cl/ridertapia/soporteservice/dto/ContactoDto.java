package cl.ridertapia.soporteservice.dto;

import cl.ridertapia.soporteservice.model.Contacto;
import cl.ridertapia.soporteservice.util.RutUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.hateoas.server.core.Relation;
import java.time.LocalDateTime;

public final class ContactoDto {

    private ContactoDto() {
    }

    @Schema(description = "Mensaje de contacto (el RUT es opcional)")
    public record Crear(
            // Opcional: solo si quien escribe tiene cuenta
            @Pattern(regexp = RutUtil.RUT_REGEX, message = "El RUT debe tener formato 12345678-9")
            @Schema(example = "12345678-5")
            String rutUsuario,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
            @Schema(example = "Pedro Suárez")
            String nombre,

            @NotBlank(message = "El email es obligatorio")
            @Email(message = "El email no es válido")
            @Size(max = 100, message = "El email no puede superar los 100 caracteres")
            @Schema(example = "pedro@correo.cl")
            String emailContacto,

            @NotBlank(message = "El asunto es obligatorio")
            @Size(max = 150, message = "El asunto no puede superar los 150 caracteres")
            @Schema(example = "Consulta por mi pedido")
            String asunto,

            @NotBlank(message = "El mensaje es obligatorio")
            @Size(max = 1000, message = "El mensaje no puede superar los 1000 caracteres")
            @Schema(example = "Hola, quiero saber el estado de mi pedido")
            String mensaje
    ) {
    }

    public record Responder(
            @NotBlank(message = "La respuesta es obligatoria")
            @Size(max = 1000, message = "La respuesta no puede superar los 1000 caracteres")
            String respuesta
    ) {
    }

    @Relation(collectionRelation = "contactos", itemRelation = "contacto")
    @Schema(description = "Mensaje de contacto")
    public record Respuesta(
            Long idContacto,
            String rutUsuario,
            String nombre,
            String emailContacto,
            String asunto,
            String mensaje,
            Contacto.Estado estado,
            String respuesta,
            LocalDateTime fecha
    ) {
        public static Respuesta desde(Contacto c) {
            return new Respuesta(c.getIdContacto(), c.getRutUsuario(), c.getNombre(), c.getEmailContacto(),
                    c.getAsunto(), c.getMensaje(), c.getEstado(), c.getRespuesta(), c.getFecha());
        }
    }
}
