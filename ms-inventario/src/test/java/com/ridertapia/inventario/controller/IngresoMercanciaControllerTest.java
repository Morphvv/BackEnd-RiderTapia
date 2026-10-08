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

import com.ridertapia.inventario.model.IngresoMercancia;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TipoMovimiento;
import com.ridertapia.inventario.service.IngresoMercanciaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(IngresoMercanciaController.class)
@ActiveProfiles("test")
class IngresoMercanciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngresoMercanciaService ingresoMercanciaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private IngresoMercancia ingresoMercancia;

    @BeforeEach
    void setUp() {
        ingresoMercancia = new IngresoMercancia(1L, TipoMovimiento.INGRESO, 10, 5, 15, LocalDateTime.of(2026, 10, 6, 9, 0), "Recepción de factura", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), 640000, "FAC-5501", 1L);
    }

    @Test
    void listarIngresosMercancia() throws Exception {
        IngresoMercancia otro = new IngresoMercancia(2L, TipoMovimiento.VENTA_WEB, 2, 15, 13, LocalDateTime.of(2026, 10, 6, 12, 0), "Venta pedido PED-0001", 2L, 6L, 4L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), 650000, "FAC-5502", 2L);
        when(ingresoMercanciaService.listarIngresosMercancia()).thenReturn(Arrays.asList(ingresoMercancia, otro));

        mockMvc.perform(get("/api/v1/ingresos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ingresoMercanciaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.ingresoMercanciaList[0].tipo").value("INGRESO"))
                .andExpect(jsonPath("$._embedded.ingresoMercanciaList[1].tipo").value("VENTA_WEB"))
                .andExpect(jsonPath("$._embedded.ingresoMercanciaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(ingresoMercanciaService.findById(1L)).thenReturn(Optional.of(ingresoMercancia));

        mockMvc.perform(get("/api/v1/ingresos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMovimientoInventario").value(1))
                .andExpect(jsonPath("$.tipo").value("INGRESO"))
                .andExpect(jsonPath("$.cantidad").value(10))
                .andExpect(jsonPath("$.stockAnterior").value(5))
                .andExpect(jsonPath("$.stockResultante").value(15))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.observacion").value("Recepción de factura"))
                .andExpect(jsonPath("$.idEmpleado").value(1))
                .andExpect(jsonPath("$.idPedido").value(5))
                .andExpect(jsonPath("$.idOrdenTrabajo").value(3))
                .andExpect(jsonPath("$.costoUnitario").value(640000))
                .andExpect(jsonPath("$.numeroDocumento").value("FAC-5501"))
                .andExpect(jsonPath("$.idProveedor").value(1))
                .andExpect(jsonPath("$.itemInventario.idItemInventario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ingresos.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(ingresoMercanciaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/ingresos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorItemInventarioConResultados() throws Exception {
        when(ingresoMercanciaService.findByItemInventario(1L)).thenReturn(List.of(ingresoMercancia));

        mockMvc.perform(get("/api/v1/ingresos/itemInventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ingresoMercanciaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.ingresoMercanciaList[0].idMovimientoInventario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorItemInventarioSinResultados() throws Exception {
        when(ingresoMercanciaService.findByItemInventario(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ingresos/itemInventario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearIngresoMercancia() throws Exception {
        when(ingresoMercanciaService.guardarIngresoMercancia(any(IngresoMercancia.class))).thenReturn(ingresoMercancia);

        mockMvc.perform(post("/api/v1/ingresos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ingresoMercancia)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimientoInventario").value(1))
                .andExpect(jsonPath("$.numeroDocumento").value("FAC-5501"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ingresos.href").exists());

        verify(ingresoMercanciaService, times(1)).guardarIngresoMercancia(any(IngresoMercancia.class));
    }

    @Test
    void crearIngresoMercanciaConRelacionNoExistente() throws Exception {
        when(ingresoMercanciaService.guardarIngresoMercancia(any(IngresoMercancia.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/ingresos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ingresoMercancia)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarIngresoMercanciaExistente() throws Exception {
        IngresoMercancia actualizado = new IngresoMercancia(1L, TipoMovimiento.AJUSTE, 3, 13, 10, LocalDateTime.of(2026, 10, 7, 9, 0), "Ajuste por conteo", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), 630000, "FAC-5503", 1L);
        when(ingresoMercanciaService.actualizarIngresoMercancia(eq(1L), any(IngresoMercancia.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/ingresos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroDocumento").value("FAC-5503"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ingresos.href").exists());
    }

    @Test
    void actualizarIngresoMercanciaNoExistente() throws Exception {
        when(ingresoMercanciaService.actualizarIngresoMercancia(eq(99L), any(IngresoMercancia.class)))
                .thenThrow(new RuntimeException("IngresoMercancia no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/ingresos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ingresoMercancia)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarIngresoMercancia() throws Exception {
        doNothing().when(ingresoMercanciaService).eliminarIngresoMercancia(1L);

        mockMvc.perform(delete("/api/v1/ingresos/1"))
                .andExpect(status().isNoContent());

        verify(ingresoMercanciaService, times(1)).eliminarIngresoMercancia(1L);
    }
}
