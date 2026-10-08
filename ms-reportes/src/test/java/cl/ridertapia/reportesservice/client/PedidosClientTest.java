package cl.ridertapia.reportesservice.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import cl.ridertapia.reportesservice.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/** Prueba el cliente HTTP contra un servicio de pedidos simulado (no abre ningún puerto). */
class PedidosClientTest {

    private static final String BASE = "http://pedidos-service";
    private static final MediaType HAL = MediaType.parseMediaType("application/hal+json");

    private MockRestServiceServer servidor;
    private PedidosClient pedidosClient;

    @BeforeEach
    void preparar() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        servidor = MockRestServiceServer.bindTo(builder).build();
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        pedidosClient = new PedidosClient(builder.build(), mapper);
    }

    private static final String PEDIDO_JSON = """
            {"idPedido":1,"numero":"P-0001","fechaCreacion":"2026-10-07T10:30:00","estado":"PAGADO","canal":"WEB",
             "subtotal":10000,"descuento":0,"total":10000,"rutCliente":"12345678-5","cuponAplicado":"X",
             "_links":{"self":{"href":"http://localhost:8086/api/v1/pedidos/1"}}}
            """;

    @Test
    @DisplayName("listarPedidos: lee la lista HAL de _embedded e ignora campos y enlaces desconocidos")
    void listarPedidos_hal() {
        String json = "{\"_embedded\":{\"pedidoList\":[" + PEDIDO_JSON + "]},\"_links\":{\"self\":{\"href\":\"x\"}}}";
        servidor.expect(requestTo(BASE + "/api/v1/pedidos"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, HAL));

        List<PedidoDto> pedidos = pedidosClient.listarPedidos();

        assertThat(pedidos).hasSize(1);
        PedidoDto p = pedidos.get(0);
        assertThat(p.idPedido()).isEqualTo(1L);
        assertThat(p.estado()).isEqualTo("PAGADO");
        assertThat(p.total()).isEqualTo(10000);
        assertThat(p.rutCliente()).isEqualTo("12345678-5");
        assertThat(p.fechaCreacion()).isEqualTo(LocalDateTime.of(2026, 10, 7, 10, 30));
        servidor.verify();
    }

    @Test
    @DisplayName("listarPedidos: también acepta un arreglo JSON simple")
    void listarPedidos_arregloSimple() {
        servidor.expect(requestTo(BASE + "/api/v1/pedidos"))
                .andRespond(withSuccess("[" + PEDIDO_JSON + "," + PEDIDO_JSON + "]", MediaType.APPLICATION_JSON));

        assertThat(pedidosClient.listarPedidos()).hasSize(2);
    }

    @Test
    @DisplayName("listarPedidos: una respuesta HAL sin _embedded es una lista vacía")
    void listarPedidos_vacia() {
        servidor.expect(requestTo(BASE + "/api/v1/pedidos"))
                .andRespond(withSuccess("{\"_links\":{\"self\":{\"href\":\"x\"}}}", HAL));

        assertThat(pedidosClient.listarPedidos()).isEmpty();
    }

    @Test
    @DisplayName("listarDetalles: lee el pedido que viene anidado dentro de cada detalle")
    void listarDetalles() {
        String detalle = "{\"idDetallePedido\":5,\"idVariante\":100,\"cantidad\":2,\"precioUnitario\":5000,"
                + "\"subtotal\":10000,\"pedido\":" + PEDIDO_JSON + "}";
        servidor.expect(requestTo(BASE + "/api/v1/detalles"))
                .andRespond(withSuccess("{\"_embedded\":{\"detallePedidoList\":[" + detalle + "]}}", HAL));

        List<DetallePedidoDto> detalles = pedidosClient.listarDetalles();

        assertThat(detalles).hasSize(1);
        assertThat(detalles.get(0).idVariante()).isEqualTo(100L);
        assertThat(detalles.get(0).cantidad()).isEqualTo(2);
        assertThat(detalles.get(0).pedido().idPedido()).isEqualTo(1L);
    }

    @Test
    @DisplayName("listarDetallesPorPedido: consulta la ruta del pedido")
    void listarDetallesPorPedido() {
        String detalle = "{\"idDetallePedido\":5,\"idVariante\":100,\"cantidad\":1,\"precioUnitario\":10000,\"subtotal\":10000}";
        servidor.expect(requestTo(BASE + "/api/v1/detalles/pedido/7"))
                .andRespond(withSuccess("{\"_embedded\":{\"detallePedidoList\":[" + detalle + "]}}", HAL));

        assertThat(pedidosClient.listarDetallesPorPedido(7L)).hasSize(1);
        servidor.verify();
    }

    @Test
    @DisplayName("obtenerPedido: lee un pedido por id")
    void obtenerPedido_ok() {
        servidor.expect(requestTo(BASE + "/api/v1/pedidos/1")).andRespond(withSuccess(PEDIDO_JSON, HAL));

        PedidoDto pedido = pedidosClient.obtenerPedido(1L);

        assertThat(pedido.numero()).isEqualTo("P-0001");
        assertThat(pedido.canal()).isEqualTo("WEB");
    }

    @Test
    @DisplayName("obtenerPedido: un 404 del servicio de pedidos se traduce a noEncontrado")
    void obtenerPedido_noExiste() {
        servidor.expect(requestTo(BASE + "/api/v1/pedidos/99")).andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> pedidosClient.obtenerPedido(99L))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("obtenerPedido y listados: un error 500 del servicio de pedidos se traduce a 503")
    void errorDelServidor() {
        servidor.expect(requestTo(BASE + "/api/v1/pedidos/1")).andRespond(withServerError());
        servidor.expect(requestTo(BASE + "/api/v1/pedidos")).andRespond(withServerError());
        servidor.expect(requestTo(BASE + "/api/v1/detalles")).andRespond(withServerError());

        assertThatThrownBy(() -> pedidosClient.obtenerPedido(1L)).isInstanceOfSatisfying(ApiException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        assertThatThrownBy(() -> pedidosClient.listarPedidos()).isInstanceOfSatisfying(ApiException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        assertThatThrownBy(() -> pedidosClient.listarDetalles()).isInstanceOfSatisfying(ApiException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }
}
