package cl.ridertapia.reportesservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.reportesservice.client.DetallePedidoDto;
import cl.ridertapia.reportesservice.client.PedidoDto;
import cl.ridertapia.reportesservice.client.PedidosClient;
import cl.ridertapia.reportesservice.dto.DocumentoDto;
import cl.ridertapia.reportesservice.exception.ApiException;
import cl.ridertapia.reportesservice.model.DocumentoVenta;
import cl.ridertapia.reportesservice.repository.DatosFacturaRepository;
import cl.ridertapia.reportesservice.repository.DetalleDocumentoRepository;
import cl.ridertapia.reportesservice.repository.DocumentoVentaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class DocumentoVentaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocumentoVentaRepository documentoRepository;

    @Autowired
    private DetalleDocumentoRepository detalleRepository;

    @Autowired
    private DatosFacturaRepository datosFacturaRepository;

    @MockBean
    private PedidosClient pedidosClient;

    @BeforeEach
    void limpiar() {
        datosFacturaRepository.deleteAll();
        detalleRepository.deleteAll();
        documentoRepository.deleteAll();
    }

    /** Deja un pedido con una sola línea en el servicio de pedidos simulado. */
    private void pedido(long id, String estado, int total, int descuento, String rutCliente) {
        PedidoDto pedido = new PedidoDto(id, "P-" + id, LocalDateTime.of(2026, 10, 7, 12, 0), estado, "WEB",
                total + descuento, descuento, total, rutCliente);
        when(pedidosClient.obtenerPedido(id)).thenReturn(pedido);
        when(pedidosClient.listarDetallesPorPedido(id)).thenReturn(List.of(
                new DetallePedidoDto(id, 100L, 1, total + descuento, total + descuento, pedido)));
    }

    private String boleta(long idPedido) throws Exception {
        return objectMapper.writeValueAsString(new DocumentoDto.EmitirBoleta(idPedido));
    }

    private String factura(long idPedido, String rut) throws Exception {
        return objectMapper.writeValueAsString(new DocumentoDto.EmitirFactura(
                idPedido, rut, "Ciclos del Sur SpA", "Venta de bicicletas", "Av. Colón 1234"));
    }

    @Test
    void testEmitirYObtenerBoleta() throws Exception {
        pedido(10, "PAGADO", 11900, 0, "12345678-5");

        mockMvc.perform(post("/api/v1/documentos/boleta")
                        .contentType(MediaType.APPLICATION_JSON).content(boleta(10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documento.idDocumento").exists())
                .andExpect(jsonPath("$.documento.tipo").value("BOLETA"))
                .andExpect(jsonPath("$.documento.numero").value(1))
                .andExpect(jsonPath("$.documento.rutCliente").value("12345678-5"))
                .andExpect(jsonPath("$.documento.neto").value(10000))
                .andExpect(jsonPath("$.documento.iva").value(1900))
                .andExpect(jsonPath("$.documento.total").value(11900))
                .andExpect(jsonPath("$.lineas", hasSize(1)))
                .andExpect(jsonPath("$.lineas[0].idVariante").value(100))
                .andExpect(jsonPath("$.datosFactura").doesNotExist())
                .andExpect(jsonPath("$._links").exists());

        Long id = documentoRepository.findAll().get(0).getIdDocumento();

        mockMvc.perform(get("/api/v1/documentos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento.numero").value(1))
                .andExpect(jsonPath("$.lineas", hasSize(1)))
                .andExpect(jsonPath("$._links.self").exists());

        mockMvc.perform(get("/api/v1/documentos/pedido/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento.idPedido").value(10));

        mockMvc.perform(get("/api/v1/documentos/buscar").param("tipo", "BOLETA").param("numero", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento.idPedido").value(10));

        mockMvc.perform(get("/api/v1/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.documentos", hasSize(1)))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void testLaNumeracionEsCorrelativaPorTipo() throws Exception {
        pedido(10, "PAGADO", 11900, 0, "12345678-5");
        pedido(11, "RETIRADO", 5950, 0, "11111111-1");
        pedido(12, "LISTO_PARA_RETIRO", 23800, 0, "22222222-2");

        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(10)))
                .andExpect(jsonPath("$.documento.numero").value(1));
        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(11)))
                .andExpect(jsonPath("$.documento.numero").value(2));
        // La primera factura vuelve a empezar en 1: cada tipo tiene su propia numeración
        mockMvc.perform(post("/api/v1/documentos/factura").contentType(MediaType.APPLICATION_JSON)
                        .content(factura(12, "76543210-3")))
                .andExpect(jsonPath("$.documento.numero").value(1));

        assertThat(documentoRepository.count()).isEqualTo(3);
    }

    @Test
    void testEmitirYObtenerFactura() throws Exception {
        pedido(10, "PAGADO", 11900, 0, "12345678-5");

        mockMvc.perform(post("/api/v1/documentos/factura")
                        .contentType(MediaType.APPLICATION_JSON).content(factura(10, "76543210-3")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documento.tipo").value("FACTURA"))
                .andExpect(jsonPath("$.datosFactura.rutEmpresa").value("76543210-3"))
                .andExpect(jsonPath("$.datosFactura.razonSocial").value("Ciclos del Sur SpA"))
                .andExpect(jsonPath("$.datosFactura.giro").value("Venta de bicicletas"))
                .andExpect(jsonPath("$._links").exists());

        // Al consultarla de nuevo, la factura sigue trayendo los datos de la empresa
        mockMvc.perform(get("/api/v1/documentos/buscar").param("tipo", "FACTURA").param("numero", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datosFactura.direccionEmpresa").value("Av. Colón 1234"));

        assertThat(datosFacturaRepository.count()).isEqualTo(1);
    }

    @Test
    void testPedidoConDescuentoGeneraLineaNegativa() throws Exception {
        pedido(10, "PAGADO", 9000, 1000, "12345678-5");

        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documento.total").value(9000))
                .andExpect(jsonPath("$.lineas", hasSize(2)))
                .andExpect(jsonPath("$.lineas[1].descripcion").value("Descuento aplicado"))
                .andExpect(jsonPath("$.lineas[1].subtotal").value(-1000));
    }

    @Test
    void testUnPedidoNoAdmiteDosDocumentos() throws Exception {
        pedido(10, "PAGADO", 11900, 0, "12345678-5");

        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(10)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(10)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("10")));
        mockMvc.perform(post("/api/v1/documentos/factura").contentType(MediaType.APPLICATION_JSON)
                        .content(factura(10, "76543210-3")))
                .andExpect(status().isConflict());

        assertThat(documentoRepository.count()).isEqualTo(1);
    }

    @Test
    void testNoSeEmiteDocumentoDePedidoSinPagar() throws Exception {
        pedido(10, "PENDIENTE_PAGO", 11900, 0, "12345678-5");

        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("PENDIENTE_PAGO")));

        assertThat(documentoRepository.count()).isZero();
    }

    @Test
    void testPedidoInexistente() throws Exception {
        when(pedidosClient.obtenerPedido(99L)).thenThrow(ApiException.noEncontrado("No existe el pedido 99"));

        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(99)))
                .andExpect(status().isNotFound());

        assertThat(documentoRepository.count()).isZero();
    }

    @Test
    void testServicioDePedidosCaido() throws Exception {
        when(pedidosClient.obtenerPedido(10L)).thenThrow(ApiException.servicioNoDisponible("pedidos caído"));

        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(10)))
                .andExpect(status().isServiceUnavailable());

        assertThat(documentoRepository.count()).isZero();
    }

    @Test
    void testFacturaConRutDeEmpresaInvalido() throws Exception {
        pedido(10, "PAGADO", 11900, 0, "12345678-5");

        // Formato correcto pero dígito verificador incorrecto
        mockMvc.perform(post("/api/v1/documentos/factura").contentType(MediaType.APPLICATION_JSON)
                        .content(factura(10, "76543210-9")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("RUT")));
        // Formato incorrecto
        mockMvc.perform(post("/api/v1/documentos/factura").contentType(MediaType.APPLICATION_JSON)
                        .content(factura(10, "123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("rutEmpresa")));

        assertThat(documentoRepository.count()).isZero();
    }

    @Test
    void testEmitirConDatosInvalidos() throws Exception {
        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("idPedido")));
    }

    @Test
    void testListarYFiltrarDocumentos() throws Exception {
        pedido(10, "PAGADO", 11900, 0, "12345678-5");
        pedido(11, "PAGADO", 5950, 0, "12345678-5");
        pedido(12, "PAGADO", 23800, 0, "33333333-3");
        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(10)));
        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(11)));
        mockMvc.perform(post("/api/v1/documentos/factura").contentType(MediaType.APPLICATION_JSON)
                .content(factura(12, "76543210-3")));

        mockMvc.perform(get("/api/v1/documentos"))
                .andExpect(jsonPath("$._embedded.documentos", hasSize(3)));
        mockMvc.perform(get("/api/v1/documentos").param("tipo", "FACTURA"))
                .andExpect(jsonPath("$._embedded.documentos", hasSize(1)))
                .andExpect(jsonPath("$._embedded.documentos[0].idPedido").value(12));
        mockMvc.perform(get("/api/v1/documentos/cliente/12345678-5"))
                .andExpect(jsonPath("$._embedded.documentos", hasSize(2)))
                .andExpect(jsonPath("$._embedded.documentos[0].rutCliente").value("12345678-5"));
        mockMvc.perform(get("/api/v1/documentos/cliente/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void testConsultasDeDocumentoInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/documentos/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(get("/api/v1/documentos/pedido/9999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/documentos/buscar").param("tipo", "BOLETA").param("numero", "99"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/documentos/buscar").param("tipo", "NOTA").param("numero", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDocumentoGuardadoEnBd() throws Exception {
        pedido(10, "PAGADO", 11900, 0, null);   // venta de visitante: sin cliente

        mockMvc.perform(post("/api/v1/documentos/boleta").contentType(MediaType.APPLICATION_JSON).content(boleta(10)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documento.rutCliente").doesNotExist());

        DocumentoVenta enBd = documentoRepository.findByIdPedido(10L).orElseThrow();
        assertThat(enBd.getNeto() + enBd.getIva()).isEqualTo(enBd.getTotal());
        assertThat(detalleRepository.findByIdDocumento(enBd.getIdDocumento())).hasSize(1);
    }
}
