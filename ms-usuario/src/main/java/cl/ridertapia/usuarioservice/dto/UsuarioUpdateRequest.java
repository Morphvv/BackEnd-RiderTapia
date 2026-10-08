package cl.ridertapia.usuarioservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para actualizar un usuario. La contraseña y el rol son opcionales. */
public record UsuarioUpdateRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        String emailCliente,

        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @Pattern(regexp = "^(ADMIN|CLIENTE)$", message = "El rol debe ser ADMIN o CLIENTE")
        String rol
) {
}
