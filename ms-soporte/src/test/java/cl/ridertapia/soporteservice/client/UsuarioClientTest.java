package cl.ridertapia.soporteservice.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import cl.ridertapia.soporteservice.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Prueba el cliente HTTP contra un usuario-service simulado (no abre ningún puerto). */
class UsuarioClientTest {

    private static final String BASE = "http://usuario-service";
    private static final String RUT = "12345678-5";
    private static final String URL = BASE + UsuarioClient.RUTA_USUARIO.replace("{rut}", RUT);

    private MockRestServiceServer servidor;
    private UsuarioClient usuarioClient;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        servidor = MockRestServiceServer.bindTo(builder).build();
        usuarioClient = new UsuarioClient(builder.build());
    }

    @Test
    @DisplayName("obtenerPorRut: lee la cuenta e ignora los enlaces HATEOAS y campos desconocidos")
    void ok() {
        String json = "{\"rutUsuario\":\"12345678-5\",\"emailCliente\":\"juan@example.com\",\"rol\":\"CLIENTE\","
                + "\"_links\":{\"self\":{\"href\":\"x\"}}}";
        servidor.expect(requestTo(URL)).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.parseMediaType("application/hal+json")));

        UsuarioDto usuario = usuarioClient.obtenerPorRut(RUT);

        assertThat(usuario.rutUsuario()).isEqualTo(RUT);
        assertThat(usuario.emailCliente()).isEqualTo("juan@example.com");
        assertThat(usuario.rol()).isEqualTo("CLIENTE");
        servidor.verify();
    }

    @Test
    @DisplayName("obtenerPorRut: 404 del otro servicio se traduce a 404 propio")
    void noExiste() {
        servidor.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> usuarioClient.obtenerPorRut(RUT))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getMessage()).contains(RUT);
                });
    }

    @Test
    @DisplayName("obtenerPorRut: si usuario-service falla responde 503")
    void caido() {
        servidor.expect(requestTo(URL)).andRespond(withServerError());

        assertThatThrownBy(() -> usuarioClient.obtenerPorRut(RUT))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    @DisplayName("obtenerPorRut: respuesta vacía se trata como cuenta inexistente")
    void vacia() {
        servidor.expect(requestTo(URL)).andRespond(withSuccess());

        assertThatThrownBy(() -> usuarioClient.obtenerPorRut(RUT))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
