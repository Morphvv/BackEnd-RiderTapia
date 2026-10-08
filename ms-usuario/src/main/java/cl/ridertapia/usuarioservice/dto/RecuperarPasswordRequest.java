package cl.ridertapia.usuarioservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecuperarPasswordRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        String emailCliente,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String nuevaPassword
) {
}
