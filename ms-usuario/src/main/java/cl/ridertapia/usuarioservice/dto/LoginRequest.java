package cl.ridertapia.usuarioservice.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El email es obligatorio") String emailCliente,
        @NotBlank(message = "La contraseña es obligatoria") String password
) {
}
