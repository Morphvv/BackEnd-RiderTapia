package cl.ridertapia.clienteservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para crear el perfil de un cliente. */
@Schema(description = "Datos para crear el perfil de un cliente")
public record ClienteRequest(
        @NotBlank(message = "El RUT es obligatorio")
        @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "El RUT debe tener formato 12345678-9")
        @Schema(example = "12345678-5") String rutUsuario,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede superar los 60 caracteres")
        @Schema(example = "Matias") String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 60, message = "El apellido no puede superar los 60 caracteres")
        @Schema(example = "Dominguez") String apellido,

        @Pattern(regexp = "^(\\+?\\d{8,12})?$", message = "El teléfono debe tener entre 8 y 12 dígitos")
        @Schema(example = "+56912345678") String telefono,

        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres")
        @Schema(example = "Calle Falsa 123") String direccion,

        @Size(max = 60, message = "La comuna no puede superar los 60 caracteres")
        @Schema(example = "Concepción") String comuna
) {
}
