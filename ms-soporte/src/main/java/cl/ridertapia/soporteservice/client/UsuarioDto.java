package cl.ridertapia.soporteservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Lo que soporte-service necesita saber de la cuenta que devuelve usuario-service. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioDto(String rutUsuario, String emailCliente, String rol) {
}
