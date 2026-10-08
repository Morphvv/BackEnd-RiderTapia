package com.ridertapia.pedidos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ridertapia.pedidos.model.Factura;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.service.FacturaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(FacturaController.class)
@ActiveProfiles("test")
class FacturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FacturaService facturaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Factura factura;

    @BeforeEach
    void setUp() {
        factura = new Factura(1L, 1001, LocalDate.of(2026, 10, 6), 756294, 143696, 899990, "https://docs.ridertapia.cl/1001.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null), "76123456-7", "Ciclismo Sur SpA", "Venta de artículos deportivos", "Barros Arana 123, Concepción");
    }

    @Test
    void listarFacturas() throws Exception {
        Factura otro = new Factura(2L, 1002, LocalDate.of(2026, 10, 7), 67218, 12772, 79990, "https://docs.ridertapia.cl/1002.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null), "77987654-3", "Ruedas del Biobío Ltda", "Taller de bicicletas", "O'Higgins 456, Concepción");
        when(facturaService.listarFacturas()).thenReturn(Arrays.asList(factura, otro));

        mockMvc.perform(get("/api/v1/facturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.facturaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.facturaList[0].folio").value(1001))
                .andExpect(jsonPath("$._embedded.facturaList[1].folio").value(1002))
                .andExpect(jsonPath("$._embedded.facturaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(facturaService.findById(1L)).thenReturn(Optional.of(factura));

        mockMvc.perform(get("/api/v1/facturas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDocumentoTributario").value(1))
                .andExpect(jsonPath("$.folio").value(1001))
                .andExpect(jsonPath("$.fechaEmision").exists())
                .andExpect(jsonPath("$.montoNeto").value(756294))
                .andExpect(jsonPath("$.iva").value(143696))
                .andExpect(jsonPath("$.total").value(899990))
                .andExpect(jsonPath("$.pdfUrl").value("https://docs.ridertapia.cl/1001.pdf"))
                .andExpect(jsonPath("$.rutEmpresa").value("76123456-7"))
                .andExpect(jsonPath("$.razonSocial").value("Ciclismo Sur SpA"))
                .andExpect(jsonPath("$.giro").value("Venta de artículos deportivos"))
                .andExpect(jsonPath("$.direccionEmpresa").value("Barros Arana 123, Concepción"))
                .andExpect(jsonPath("$.pedido.idPedido").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.facturas.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(facturaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/facturas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorPedidoConResultados() throws Exception {
        when(facturaService.findByPedido(1L)).thenReturn(List.of(factura));

        mockMvc.perform(get("/api/v1/facturas/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.facturaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.facturaList[0].idDocumentoTributario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorPedidoSinResultados() throws Exception {
        when(facturaService.findByPedido(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/facturas/pedido/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearFactura() throws Exception {
        when(facturaService.guardarFactura(any(Factura.class))).thenReturn(factura);

        mockMvc.perform(post("/api/v1/facturas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(factura)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDocumentoTributario").value(1))
                .andExpect(jsonPath("$.razonSocial").value("Ciclismo Sur SpA"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.facturas.href").exists());

        verify(facturaService, times(1)).guardarFactura(any(Factura.class));
    }

    @Test
    void crearFacturaConRelacionNoExistente() throws Exception {
        when(facturaService.guardarFactura(any(Factura.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/facturas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(factura)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarFacturaExistente() throws Exception {
        Factura actualizado = new Factura(1L, 1003, LocalDate.of(2026, 10, 8), 922681, 175309, 1097990, "https://docs.ridertapia.cl/1003.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null), "76123456-7", "Ciclismo Sur Limitada", "Comercio", "Colo Colo 789, Concepción");
        when(facturaService.actualizarFactura(eq(1L), any(Factura.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/facturas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razonSocial").value("Ciclismo Sur Limitada"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.facturas.href").exists());
    }

    @Test
    void actualizarFacturaNoExistente() throws Exception {
        when(facturaService.actualizarFactura(eq(99L), any(Factura.class)))
                .thenThrow(new RuntimeException("Factura no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/facturas/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(factura)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarFactura() throws Exception {
        doNothing().when(facturaService).eliminarFactura(1L);

        mockMvc.perform(delete("/api/v1/facturas/1"))
                .andExpect(status().isNoContent());

        verify(facturaService, times(1)).eliminarFactura(1L);
    }
}
