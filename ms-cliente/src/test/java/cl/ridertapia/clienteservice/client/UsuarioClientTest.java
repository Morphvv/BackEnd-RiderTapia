package cl.ridertapia.clienteservice.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import cl.ridertapia.clienteservice.dto.UsuarioDto;
import cl.ridertapia.clienteservice.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;


class UsuarioClientTest {

    private static final String BASE = "http://usuario-service";

    private MockRestServiceServer servidor;
    private UsuarioClient usuarioClient;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        servidor = MockRestServiceServer.bindTo(builder).build();
        usuarioClient = new UsuarioClient(builder.build());
    }

    @Test
    @DisplayName("obtenerPorRut: lee la cuenta e ignora los _links de HATEOAS")
    void obtenerPorRut_ok() {
        String json = """
                {"rutUsuario":"12345678-5","emailCliente":"Matias@correo.cl","rol":"CLIENTE",
                 "_links":{"self":{"href":"http://localhost:8081/api/v1/usuarios/12345678-5"}}}
                """;
        servidor.expect(requestTo(BASE + "/api/v1/usuarios/12345678-5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.parseMediaType("application/hal+json")));

        UsuarioDto cuenta = usuarioClient.obtenerPorRut("12345678-5");

        assertThat(cuenta.rutUsuario()).isEqualTo("12345678-5");
        assertThat(cuenta.rol()).isEqualTo("CLIENTE");
        servidor.verify();
    }

    @Test
    @DisplayName("obtenerPorRut: 404 de usuario-service se traduce a noEncontrado")
    void obtenerPorRut_noExiste() {
        servidor.expect(requestTo(BASE + "/api/v1/usuarios/12345678-5"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> usuarioClient.obtenerPorRut("12345678-5"))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("obtenerPorRut: error 500 de usuario-service se traduce a 503")
    void obtenerPorRut_errorDelServidor() {
        servidor.expect(requestTo(BASE + "/api/v1/usuarios/12345678-5"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> usuarioClient.obtenerPorRut("12345678-5"))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }
}
