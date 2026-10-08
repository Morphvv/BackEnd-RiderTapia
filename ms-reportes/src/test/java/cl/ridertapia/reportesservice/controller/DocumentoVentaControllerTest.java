package cl.ridertapia.reportesservice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.reportesservice.dto.DocumentoDto;
import cl.ridertapia.reportesservice.exception.ApiException;
import cl.ridertapia.reportesservice.model.DocumentoVenta;
import cl.ridertapia.reportesservice.service.DocumentoVentaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocumentoVentaController.class)
@Import(DocumentoModelAssembler.class)
class DocumentoVentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DocumentoVentaService documentoService;

    private DocumentoDto.Resumen resumen(DocumentoVenta.Tipo tipo) {
        return new DocumentoDto.Resumen(1L, 1L, tipo, 10L, "12345678-5",
                LocalDateTime.of(2026, 10, 7, 12, 0), 10000, 1900, 11900);
    }

    private DocumentoDto.Detalle boleta() {
        return new DocumentoDto.Detalle(resumen(DocumentoVenta.Tipo.BOLETA),
                List.of(new DocumentoDto.Linea(100L, "Variante 100", 1, 11900, 11900)), null);
    }

    private DocumentoDto.Detalle factura() {
        return new DocumentoDto.Detalle(resumen(DocumentoVenta.Tipo.FACTURA),
                List.of(new DocumentoDto.Linea(100L, "Variante 100", 1, 11900, 11900)),
                new DocumentoDto.DatosEmpresa("76543210-3", "Ciclos del Sur SpA", "Venta de bicicletas", "Av. Colón 1234"));
    }

    private String json(Object cuerpo) throws Exception {
        return objectMapper.writeValueAsString(cuerpo);
    }

    // ---------- emitir ----------

    @Test
    @DisplayName("POST /api/v1/documentos/boleta → 201, Location, montos y enlaces")
    void emitirBoleta() throws Exception {
        when(documentoService.emitirBoleta(any(DocumentoDto.EmitirBoleta.class))).thenReturn(boleta());

        mockMvc.perform(post("/api/v1/documentos/boleta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new DocumentoDto.EmitirBoleta(10L))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/documentos/1")))
                .andExpect(jsonPath("$.documento.tipo").value("BOLETA"))
                .andExpect(jsonPath("$.documento.neto").value(10000))
                .andExpect(jsonPath("$.documento.iva").value(1900))
                .andExpect(jsonPath("$.documento.total").value(11900))
                .andExpect(jsonPath("$.lineas", hasSize(1)))
                .andExpect(jsonPath("$.datosFactura").doesNotExist())
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/documentos/1")))
                .andExpect(jsonPath("$._links.pedido.href", endsWith("/api/v1/documentos/pedido/10")))
                .andExpect(jsonPath("$._links.documentos.href", endsWith("/api/v1/documentos{?tipo}")));
    }

    @Test
    @DisplayName("POST /api/v1/documentos/boleta sin idPedido → 400")
    void emitirBoleta_validacion() throws Exception {
        mockMvc.perform(post("/api/v1/documentos/boleta")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("idPedido")));
    }

    @Test
    @DisplayName("POST /api/v1/documentos/boleta de un pedido ya facturado → 409")
    void emitirBoleta_conflicto() throws Exception {
        when(documentoService.emitirBoleta(any(DocumentoDto.EmitirBoleta.class)))
                .thenThrow(ApiException.conflicto("El pedido 10 ya tiene un documento emitido"));

        mockMvc.perform(post("/api/v1/documentos/boleta")
                        .contentType(MediaType.APPLICATION_JSON).content(json(new DocumentoDto.EmitirBoleta(10L))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/v1/documentos/boleta de un pedido inexistente → 404")
    void emitirBoleta_pedidoInexistente() throws Exception {
        when(documentoService.emitirBoleta(any(DocumentoDto.EmitirBoleta.class)))
                .thenThrow(ApiException.noEncontrado("No existe el pedido 10"));

        mockMvc.perform(post("/api/v1/documentos/boleta")
                        .contentType(MediaType.APPLICATION_JSON).content(json(new DocumentoDto.EmitirBoleta(10L))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/documentos/boleta de un pedido sin pagar → 400")
    void emitirBoleta_pedidoSinPagar() throws Exception {
        when(documentoService.emitirBoleta(any(DocumentoDto.EmitirBoleta.class)))
                .thenThrow(ApiException.solicitudInvalida("Solo se emiten documentos de pedidos pagados"));

        mockMvc.perform(post("/api/v1/documentos/boleta")
                        .contentType(MediaType.APPLICATION_JSON).content(json(new DocumentoDto.EmitirBoleta(10L))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/documentos/factura → 201 con los datos de la empresa")
    void emitirFactura() throws Exception {
        when(documentoService.emitirFactura(any(DocumentoDto.EmitirFactura.class))).thenReturn(factura());
        DocumentoDto.EmitirFactura cuerpo = new DocumentoDto.EmitirFactura(
                10L, "76543210-3", "Ciclos del Sur SpA", "Venta de bicicletas", "Av. Colón 1234");

        mockMvc.perform(post("/api/v1/documentos/factura")
                        .contentType(MediaType.APPLICATION_JSON).content(json(cuerpo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documento.tipo").value("FACTURA"))
                .andExpect(jsonPath("$.datosFactura.rutEmpresa").value("76543210-3"))
                .andExpect(jsonPath("$.datosFactura.razonSocial").value("Ciclos del Sur SpA"))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    @DisplayName("POST /api/v1/documentos/factura con datos inválidos → 400 con el detalle de cada campo")
    void emitirFactura_validacion() throws Exception {
        DocumentoDto.EmitirFactura invalida = new DocumentoDto.EmitirFactura(null, "123", "", " ", null);

        mockMvc.perform(post("/api/v1/documentos/factura")
                        .contentType(MediaType.APPLICATION_JSON).content(json(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("idPedido")))
                .andExpect(jsonPath("$.mensaje", containsString("rutEmpresa")))
                .andExpect(jsonPath("$.mensaje", containsString("razonSocial")))
                .andExpect(jsonPath("$.mensaje", containsString("giro")))
                .andExpect(jsonPath("$.mensaje", containsString("direccionEmpresa")));
    }

    @Test
    @DisplayName("POST con un cuerpo que no es JSON válido → 400")
    void emitir_jsonInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/documentos/boleta")
                        .contentType(MediaType.APPLICATION_JSON).content("{ no es json"))
                .andExpect(status().isBadRequest());
    }

    // ---------- consultar ----------

    @Test
    @DisplayName("GET /api/v1/documentos → colección HAL")
    void listarDocumentos() throws Exception {
        when(documentoService.listarDocumentos(null)).thenReturn(List.of(
                resumen(DocumentoVenta.Tipo.BOLETA), resumen(DocumentoVenta.Tipo.FACTURA)));

        mockMvc.perform(get("/api/v1/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.documentos", hasSize(2)))
                .andExpect(jsonPath("$._embedded.documentos[0].tipo").value("BOLETA"))
                .andExpect(jsonPath("$._embedded.documentos[0]._links.self.href", endsWith("/api/v1/documentos/1")))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    @DisplayName("GET /api/v1/documentos?tipo=FACTURA → filtra por tipo")
    void listarDocumentos_filtrado() throws Exception {
        when(documentoService.listarDocumentos(DocumentoVenta.Tipo.FACTURA))
                .thenReturn(List.of(resumen(DocumentoVenta.Tipo.FACTURA)));

        mockMvc.perform(get("/api/v1/documentos").param("tipo", "FACTURA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.documentos", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/documentos?tipo=NOTA → 400 (tipo no existe)")
    void listarDocumentos_tipoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/documentos").param("tipo", "NOTA")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/documentos/{id} → documento completo")
    void findById() throws Exception {
        when(documentoService.findById(1L)).thenReturn(factura());

        mockMvc.perform(get("/api/v1/documentos/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento.numero").value(1))
                .andExpect(jsonPath("$.datosFactura.giro").value("Venta de bicicletas"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/documentos/1")));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/{id} inexistente → 404")
    void findById_noExiste() throws Exception {
        when(documentoService.findById(99L)).thenThrow(ApiException.noEncontrado("No existe el documento 99"));

        mockMvc.perform(get("/api/v1/documentos/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el documento 99"));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/pedido/{idPedido} → documento del pedido")
    void findByPedido() throws Exception {
        when(documentoService.findByPedido(10L)).thenReturn(boleta());

        mockMvc.perform(get("/api/v1/documentos/pedido/{idPedido}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento.idPedido").value(10));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/pedido/{idPedido} sin documento → 404")
    void findByPedido_noExiste() throws Exception {
        when(documentoService.findByPedido(10L))
                .thenThrow(ApiException.noEncontrado("El pedido 10 no tiene documento emitido"));

        mockMvc.perform(get("/api/v1/documentos/pedido/{idPedido}", 10)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/documentos/buscar?tipo=&numero= → busca por tipo y número")
    void findByNumero() throws Exception {
        when(documentoService.findByNumero(DocumentoVenta.Tipo.BOLETA, 1L)).thenReturn(boleta());

        mockMvc.perform(get("/api/v1/documentos/buscar").param("tipo", "BOLETA").param("numero", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento.tipo").value("BOLETA"));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/buscar sin parámetros o con tipo inválido → 400")
    void findByNumero_parametrosInvalidos() throws Exception {
        mockMvc.perform(get("/api/v1/documentos/buscar").param("numero", "1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/documentos/buscar").param("tipo", "NOTA").param("numero", "1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/documentos/cliente/{rut} → documentos del cliente")
    void findByRutCliente() throws Exception {
        when(documentoService.findByRutCliente("12345678-5")).thenReturn(List.of(resumen(DocumentoVenta.Tipo.BOLETA)));

        mockMvc.perform(get("/api/v1/documentos/cliente/{rut}", "12345678-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.documentos", hasSize(1)))
                .andExpect(jsonPath("$._embedded.documentos[0].rutCliente").value("12345678-5"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/documentos/cliente/12345678-5")));
    }

    @Test
    @DisplayName("GET /api/v1/documentos/cliente/{rut} sin documentos → 200 sin _embedded")
    void findByRutCliente_vacio() throws Exception {
        when(documentoService.findByRutCliente("33333333-3")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/documentos/cliente/{rut}", "33333333-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }
}
