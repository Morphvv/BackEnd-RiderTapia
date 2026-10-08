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

import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.MovimientoInventario;
import com.ridertapia.inventario.model.TipoMovimiento;
import com.ridertapia.inventario.service.MovimientoInventarioService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(MovimientoInventarioController.class)
@ActiveProfiles("test")
class MovimientoInventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovimientoInventarioService movimientoInventarioService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private MovimientoInventario movimientoInventario;

    @BeforeEach
    void setUp() {
        movimientoInventario = new MovimientoInventario(1L, TipoMovimiento.INGRESO, 10, 5, 15, LocalDateTime.of(2026, 10, 6, 9, 0), "Recepción de factura", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    @Test
    void listarMovimientosInventario() throws Exception {
        MovimientoInventario otro = new MovimientoInventario(2L, TipoMovimiento.VENTA_WEB, 2, 15, 13, LocalDateTime.of(2026, 10, 6, 12, 0), "Venta pedido PED-0001", 2L, 6L, 4L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(movimientoInventarioService.listarMovimientosInventario()).thenReturn(Arrays.asList(movimientoInventario, otro));

        mockMvc.perform(get("/api/v1/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.movimientoInventarioList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.movimientoInventarioList[0].tipo").value("INGRESO"))
                .andExpect(jsonPath("$._embedded.movimientoInventarioList[1].tipo").value("VENTA_WEB"))
                .andExpect(jsonPath("$._embedded.movimientoInventarioList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(movimientoInventarioService.findById(1L)).thenReturn(Optional.of(movimientoInventario));

        mockMvc.perform(get("/api/v1/movimientos/1"))
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
                .andExpect(jsonPath("$.itemInventario.idItemInventario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.movimientos.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(movimientoInventarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/movimientos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorItemInventarioConResultados() throws Exception {
        when(movimientoInventarioService.findByItemInventario(1L)).thenReturn(List.of(movimientoInventario));

        mockMvc.perform(get("/api/v1/movimientos/itemInventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.movimientoInventarioList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.movimientoInventarioList[0].idMovimientoInventario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorItemInventarioSinResultados() throws Exception {
        when(movimientoInventarioService.findByItemInventario(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/movimientos/itemInventario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearMovimientoInventario() throws Exception {
        when(movimientoInventarioService.guardarMovimientoInventario(any(MovimientoInventario.class))).thenReturn(movimientoInventario);

        mockMvc.perform(post("/api/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movimientoInventario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimientoInventario").value(1))
                .andExpect(jsonPath("$.cantidad").value(10))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.movimientos.href").exists());

        verify(movimientoInventarioService, times(1)).guardarMovimientoInventario(any(MovimientoInventario.class));
    }

    @Test
    void crearMovimientoInventarioConRelacionNoExistente() throws Exception {
        when(movimientoInventarioService.guardarMovimientoInventario(any(MovimientoInventario.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movimientoInventario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarMovimientoInventarioExistente() throws Exception {
        MovimientoInventario actualizado = new MovimientoInventario(1L, TipoMovimiento.AJUSTE, 3, 13, 10, LocalDateTime.of(2026, 10, 7, 9, 0), "Ajuste por conteo", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(movimientoInventarioService.actualizarMovimientoInventario(eq(1L), any(MovimientoInventario.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/movimientos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.movimientos.href").exists());
    }

    @Test
    void actualizarMovimientoInventarioNoExistente() throws Exception {
        when(movimientoInventarioService.actualizarMovimientoInventario(eq(99L), any(MovimientoInventario.class)))
                .thenThrow(new RuntimeException("MovimientoInventario no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/movimientos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(movimientoInventario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarMovimientoInventario() throws Exception {
        doNothing().when(movimientoInventarioService).eliminarMovimientoInventario(1L);

        mockMvc.perform(delete("/api/v1/movimientos/1"))
                .andExpect(status().isNoContent());

        verify(movimientoInventarioService, times(1)).eliminarMovimientoInventario(1L);
    }
}
