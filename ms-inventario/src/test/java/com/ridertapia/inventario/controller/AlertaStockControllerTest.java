package com.ridertapia.inventario.controller;

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

import com.ridertapia.inventario.model.AlertaStock;
import com.ridertapia.inventario.model.ItemInventario;

import com.ridertapia.inventario.service.AlertaStockService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AlertaStockController.class)
@ActiveProfiles("test")
class AlertaStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlertaStockService alertaStockService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private AlertaStock alertaStock;

    @BeforeEach
    void setUp() {
        alertaStock = new AlertaStock(1L, LocalDateTime.of(2026, 10, 6, 9, 0), 2, false, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    @Test
    void listarAlertasStock() throws Exception {
        AlertaStock otro = new AlertaStock(2L, LocalDateTime.of(2026, 10, 6, 12, 0), 1, false, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(alertaStockService.listarAlertasStock()).thenReturn(Arrays.asList(alertaStock, otro));

        mockMvc.perform(get("/api/v1/alertas-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.alertaStockList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.alertaStockList[0].fechaGeneracion").exists())
                .andExpect(jsonPath("$._embedded.alertaStockList[1].fechaGeneracion").exists())
                .andExpect(jsonPath("$._embedded.alertaStockList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(alertaStockService.findById(1L)).thenReturn(Optional.of(alertaStock));

        mockMvc.perform(get("/api/v1/alertas-stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlertaStock").value(1))
                .andExpect(jsonPath("$.fechaGeneracion").exists())
                .andExpect(jsonPath("$.stockAlMomento").value(2))
                .andExpect(jsonPath("$.atendida").value(false))
                .andExpect(jsonPath("$.itemInventario.idItemInventario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.alertasStock.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(alertaStockService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/alertas-stock/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorItemInventarioConResultados() throws Exception {
        when(alertaStockService.findByItemInventario(1L)).thenReturn(List.of(alertaStock));

        mockMvc.perform(get("/api/v1/alertas-stock/itemInventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.alertaStockList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.alertaStockList[0].idAlertaStock").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorItemInventarioSinResultados() throws Exception {
        when(alertaStockService.findByItemInventario(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/alertas-stock/itemInventario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearAlertaStock() throws Exception {
        when(alertaStockService.guardarAlertaStock(any(AlertaStock.class))).thenReturn(alertaStock);

        mockMvc.perform(post("/api/v1/alertas-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alertaStock)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAlertaStock").value(1))
                .andExpect(jsonPath("$.stockAlMomento").value(2))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.alertasStock.href").exists());

        verify(alertaStockService, times(1)).guardarAlertaStock(any(AlertaStock.class));
    }

    @Test
    void crearAlertaStockConRelacionNoExistente() throws Exception {
        when(alertaStockService.guardarAlertaStock(any(AlertaStock.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/alertas-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alertaStock)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarAlertaStockExistente() throws Exception {
        AlertaStock actualizado = new AlertaStock(1L, LocalDateTime.of(2026, 10, 7, 9, 0), 0, true, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(alertaStockService.actualizarAlertaStock(eq(1L), any(AlertaStock.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/alertas-stock/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockAlMomento").value(0))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.alertasStock.href").exists());
    }

    @Test
    void actualizarAlertaStockNoExistente() throws Exception {
        when(alertaStockService.actualizarAlertaStock(eq(99L), any(AlertaStock.class)))
                .thenThrow(new RuntimeException("AlertaStock no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/alertas-stock/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(alertaStock)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarAlertaStock() throws Exception {
        doNothing().when(alertaStockService).eliminarAlertaStock(1L);

        mockMvc.perform(delete("/api/v1/alertas-stock/1"))
                .andExpect(status().isNoContent());

        verify(alertaStockService, times(1)).eliminarAlertaStock(1L);
    }
}
