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

import com.ridertapia.inventario.model.AjusteInventario;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.model.EstadoToma;
import com.ridertapia.inventario.model.MotivoAjuste;
import com.ridertapia.inventario.model.TipoMovimiento;
import com.ridertapia.inventario.service.AjusteInventarioService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AjusteInventarioController.class)
@ActiveProfiles("test")
class AjusteInventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AjusteInventarioService ajusteInventarioService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private AjusteInventario ajusteInventario;

    @BeforeEach
    void setUp() {
        ajusteInventario = new AjusteInventario(1L, TipoMovimiento.INGRESO, 10, 5, 15, LocalDateTime.of(2026, 10, 6, 9, 0), "Recepción de factura", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), MotivoAjuste.MERMA, null);
    }

    @Test
    void listarAjustesInventario() throws Exception {
        AjusteInventario otro = new AjusteInventario(2L, TipoMovimiento.VENTA_WEB, 2, 15, 13, LocalDateTime.of(2026, 10, 6, 12, 0), "Venta pedido PED-0001", 2L, 6L, 4L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), MotivoAjuste.ERROR_CONTEO, null);
        when(ajusteInventarioService.listarAjustesInventario()).thenReturn(Arrays.asList(ajusteInventario, otro));

        mockMvc.perform(get("/api/v1/ajustes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ajusteInventarioList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.ajusteInventarioList[0].tipo").value("INGRESO"))
                .andExpect(jsonPath("$._embedded.ajusteInventarioList[1].tipo").value("VENTA_WEB"))
                .andExpect(jsonPath("$._embedded.ajusteInventarioList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(ajusteInventarioService.findById(1L)).thenReturn(Optional.of(ajusteInventario));

        mockMvc.perform(get("/api/v1/ajustes/1"))
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
                .andExpect(jsonPath("$.motivo").value("MERMA"))
                .andExpect(jsonPath("$.itemInventario.idItemInventario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ajustes.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(ajusteInventarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/ajustes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorItemInventarioConResultados() throws Exception {
        when(ajusteInventarioService.findByItemInventario(1L)).thenReturn(List.of(ajusteInventario));

        mockMvc.perform(get("/api/v1/ajustes/itemInventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ajusteInventarioList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.ajusteInventarioList[0].idMovimientoInventario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorItemInventarioSinResultados() throws Exception {
        when(ajusteInventarioService.findByItemInventario(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ajustes/itemInventario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearAjusteInventario() throws Exception {
        when(ajusteInventarioService.guardarAjusteInventario(any(AjusteInventario.class))).thenReturn(ajusteInventario);

        mockMvc.perform(post("/api/v1/ajustes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ajusteInventario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimientoInventario").value(1))
                .andExpect(jsonPath("$.motivo").value("MERMA"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ajustes.href").exists());

        verify(ajusteInventarioService, times(1)).guardarAjusteInventario(any(AjusteInventario.class));
    }

    @Test
    void crearAjusteInventarioConRelacionNoExistente() throws Exception {
        when(ajusteInventarioService.guardarAjusteInventario(any(AjusteInventario.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/ajustes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ajusteInventario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarAjusteInventarioExistente() throws Exception {
        AjusteInventario actualizado = new AjusteInventario(1L, TipoMovimiento.AJUSTE, 3, 13, 10, LocalDateTime.of(2026, 10, 7, 9, 0), "Ajuste por conteo", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), MotivoAjuste.CONTEO_FISICO, null);
        when(ajusteInventarioService.actualizarAjusteInventario(eq(1L), any(AjusteInventario.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/ajustes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").value("CONTEO_FISICO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ajustes.href").exists());
    }

    @Test
    void actualizarAjusteInventarioNoExistente() throws Exception {
        when(ajusteInventarioService.actualizarAjusteInventario(eq(99L), any(AjusteInventario.class)))
                .thenThrow(new RuntimeException("AjusteInventario no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/ajustes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ajusteInventario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarAjusteInventario() throws Exception {
        doNothing().when(ajusteInventarioService).eliminarAjusteInventario(1L);

        mockMvc.perform(delete("/api/v1/ajustes/1"))
                .andExpect(status().isNoContent());

        verify(ajusteInventarioService, times(1)).eliminarAjusteInventario(1L);
    }
}
