package cl.ridertapia.soporteservice.client;

import cl.ridertapia.soporteservice.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente HTTP hacia usuario-service. */
@Component
@RequiredArgsConstructor
public class UsuarioClient {

    /** Ruta de la cuenta en usuario-service. Si allá cambia (por ejemplo a /api/v1/usuarios), se cambia solo aquí. */
    public static final String RUTA_USUARIO = "/api/v1/usuarios/{rut}";

    private final RestClient usuarioRestClient;

    /**
     * Comprueba que el RUT tenga una cuenta y la devuelve.
     *
     * @throws ApiException 404 si no existe la cuenta, 503 si usuario-service no está disponible
     */
    public UsuarioDto obtenerPorRut(String rut) {
        try {
            UsuarioDto usuario = usuarioRestClient.get()
                    .uri(RUTA_USUARIO, rut)
                    .retrieve()
                    .body(UsuarioDto.class);
            if (usuario == null) {
                throw ApiException.noEncontrado("No existe una cuenta de usuario con el RUT " + rut);
            }
            return usuario;
        } catch (HttpClientErrorException.NotFound e) {
            throw ApiException.noEncontrado("No existe una cuenta de usuario con el RUT " + rut);
        } catch (RestClientException e) {
            throw ApiException.servicioNoDisponible(
                    "No se pudo validar el RUT: el servicio de usuarios no está disponible");
        }
    }
}
