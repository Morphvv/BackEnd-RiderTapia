package cl.ridertapia.usuarioservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para registrar un usuario nuevo. */
@Schema(description = "Datos para registrar un usuario")
public record UsuarioRequest(
        @NotBlank(message = "El RUT es obligatorio")
        @Pattern(regexp = "^\\d{7,8}-[\\dkK]$", message = "El RUT debe tener formato 12345678-9")
        @Schema(example = "12345678-5") String rutUsuario,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        @Schema(example = "pedro@correo.cl") String emailCliente,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @Schema(example = "clave12345", description = "Mínimo 8 caracteres") String password,

        @Pattern(regexp = "^(ADMIN|CLIENTE)$", message = "El rol debe ser ADMIN o CLIENTE")
        @Schema(example = "CLIENTE", description = "ADMIN o CLIENTE (por defecto CLIENTE)") String rol
) {
}
