package cl.ridertapia.clienteservice.client;

import cl.ridertapia.clienteservice.dto.UsuarioDto;
import cl.ridertapia.clienteservice.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Cliente HTTP hacia usuario-service. */
@Component
@RequiredArgsConstructor
public class UsuarioClient {

    private final RestClient usuarioRestClient;

    /**
     * Consulta la cuenta por RUT.
     *
     * @throws ApiException 404 si no existe la cuenta, 503 si usuario-service no está disponible
     */
    public UsuarioDto obtenerPorRut(String rut) {
        try {
            UsuarioDto usuario = usuarioRestClient.get()
                    .uri("/api/v1/usuarios/{rut}", rut)
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
