package cl.ridertapia.usuarioservice.dto;

import cl.ridertapia.usuarioservice.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

/** Respuesta pública: nunca incluye la contraseña. */
@Relation(collectionRelation = "usuarios", itemRelation = "usuario")
@Schema(description = "Datos públicos de un usuario")
public record UsuarioResponse(
        @Schema(example = "12345678-5") String rutUsuario,
        @Schema(example = "pedro@correo.cl") String emailCliente,
        @Schema(example = "CLIENTE", allowableValues = {"ADMIN", "CLIENTE"}) String rol) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(usuario.getRutUsuario(), usuario.getEmailCliente(), usuario.getRol());
    }
}
