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

import com.ridertapia.pedidos.model.Boleta;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.service.BoletaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(BoletaController.class)
@ActiveProfiles("test")
class BoletaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoletaService boletaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Boleta boleta;

    @BeforeEach
    void setUp() {
        boleta = new Boleta(1L, 1001, LocalDate.of(2026, 10, 6), 756294, 143696, 899990, "https://docs.ridertapia.cl/1001.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    @Test
    void listarBoletas() throws Exception {
        Boleta otro = new Boleta(2L, 1002, LocalDate.of(2026, 10, 7), 67218, 12772, 79990, "https://docs.ridertapia.cl/1002.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(boletaService.listarBoletas()).thenReturn(Arrays.asList(boleta, otro));

        mockMvc.perform(get("/api/v1/boletas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.boletaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.boletaList[0].folio").value(1001))
                .andExpect(jsonPath("$._embedded.boletaList[1].folio").value(1002))
                .andExpect(jsonPath("$._embedded.boletaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(boletaService.findById(1L)).thenReturn(Optional.of(boleta));

        mockMvc.perform(get("/api/v1/boletas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDocumentoTributario").value(1))
                .andExpect(jsonPath("$.folio").value(1001))
                .andExpect(jsonPath("$.fechaEmision").exists())
                .andExpect(jsonPath("$.montoNeto").value(756294))
                .andExpect(jsonPath("$.iva").value(143696))
                .andExpect(jsonPath("$.total").value(899990))
                .andExpect(jsonPath("$.pdfUrl").value("https://docs.ridertapia.cl/1001.pdf"))
                .andExpect(jsonPath("$.pedido.idPedido").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.boletas.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(boletaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/boletas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorPedidoConResultados() throws Exception {
        when(boletaService.findByPedido(1L)).thenReturn(List.of(boleta));

        mockMvc.perform(get("/api/v1/boletas/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.boletaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.boletaList[0].idDocumentoTributario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorPedidoSinResultados() throws Exception {
        when(boletaService.findByPedido(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/boletas/pedido/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearBoleta() throws Exception {
        when(boletaService.guardarBoleta(any(Boleta.class))).thenReturn(boleta);

        mockMvc.perform(post("/api/v1/boletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boleta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDocumentoTributario").value(1))
                .andExpect(jsonPath("$.folio").value(1001))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.boletas.href").exists());

        verify(boletaService, times(1)).guardarBoleta(any(Boleta.class));
    }

    @Test
    void crearBoletaConRelacionNoExistente() throws Exception {
        when(boletaService.guardarBoleta(any(Boleta.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/boletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boleta)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarBoletaExistente() throws Exception {
        Boleta actualizado = new Boleta(1L, 1003, LocalDate.of(2026, 10, 8), 922681, 175309, 1097990, "https://docs.ridertapia.cl/1003.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(boletaService.actualizarBoleta(eq(1L), any(Boleta.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/boletas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.folio").value(1003))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.boletas.href").exists());
    }

    @Test
    void actualizarBoletaNoExistente() throws Exception {
        when(boletaService.actualizarBoleta(eq(99L), any(Boleta.class)))
                .thenThrow(new RuntimeException("Boleta no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/boletas/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(boleta)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarBoleta() throws Exception {
        doNothing().when(boletaService).eliminarBoleta(1L);

        mockMvc.perform(delete("/api/v1/boletas/1"))
                .andExpect(status().isNoContent());

        verify(boletaService, times(1)).eliminarBoleta(1L);
    }
}
