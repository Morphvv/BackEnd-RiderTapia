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

import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.model.EstadoToma;
import com.ridertapia.inventario.service.TomaInventarioService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(TomaInventarioController.class)
@ActiveProfiles("test")
class TomaInventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TomaInventarioService tomaInventarioService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private TomaInventario tomaInventario;

    @BeforeEach
    void setUp() {
        tomaInventario = new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L);
    }

    @Test
    void listarTomasInventario() throws Exception {
        TomaInventario otro = new TomaInventario(2L, LocalDateTime.of(2026, 10, 6, 12, 0), LocalDateTime.of(2026, 10, 6, 19, 0), EstadoToma.EN_CURSO, 2L);
        when(tomaInventarioService.listarTomasInventario()).thenReturn(Arrays.asList(tomaInventario, otro));

        mockMvc.perform(get("/api/v1/tomas-inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tomaInventarioList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.tomaInventarioList[0].fechaInicio").exists())
                .andExpect(jsonPath("$._embedded.tomaInventarioList[1].fechaInicio").exists())
                .andExpect(jsonPath("$._embedded.tomaInventarioList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(tomaInventarioService.findById(1L)).thenReturn(Optional.of(tomaInventario));

        mockMvc.perform(get("/api/v1/tomas-inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTomaInventario").value(1))
                .andExpect(jsonPath("$.fechaInicio").exists())
                .andExpect(jsonPath("$.fechaCierre").exists())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$.idEmpleado").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.tomasInventario.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(tomaInventarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/tomas-inventario/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorIdEmpleadoConResultados() throws Exception {
        when(tomaInventarioService.findByIdEmpleado(1L)).thenReturn(List.of(tomaInventario));

        mockMvc.perform(get("/api/v1/tomas-inventario/idEmpleado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tomaInventarioList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.tomaInventarioList[0].idTomaInventario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdEmpleadoSinResultados() throws Exception {
        when(tomaInventarioService.findByIdEmpleado(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tomas-inventario/idEmpleado/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearTomaInventario() throws Exception {
        when(tomaInventarioService.guardarTomaInventario(any(TomaInventario.class))).thenReturn(tomaInventario);

        mockMvc.perform(post("/api/v1/tomas-inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tomaInventario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTomaInventario").value(1))
                .andExpect(jsonPath("$.estado").value("EN_CURSO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.tomasInventario.href").exists());

        verify(tomaInventarioService, times(1)).guardarTomaInventario(any(TomaInventario.class));
    }

    @Test
    void actualizarTomaInventarioExistente() throws Exception {
        TomaInventario actualizado = new TomaInventario(1L, LocalDateTime.of(2026, 10, 7, 9, 0), LocalDateTime.of(2026, 10, 7, 18, 0), EstadoToma.CONFIRMADA, 1L);
        when(tomaInventarioService.actualizarTomaInventario(eq(1L), any(TomaInventario.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/tomas-inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.tomasInventario.href").exists());
    }

    @Test
    void actualizarTomaInventarioNoExistente() throws Exception {
        when(tomaInventarioService.actualizarTomaInventario(eq(99L), any(TomaInventario.class)))
                .thenThrow(new RuntimeException("TomaInventario no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/tomas-inventario/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tomaInventario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarTomaInventario() throws Exception {
        doNothing().when(tomaInventarioService).eliminarTomaInventario(1L);

        mockMvc.perform(delete("/api/v1/tomas-inventario/1"))
                .andExpect(status().isNoContent());

        verify(tomaInventarioService, times(1)).eliminarTomaInventario(1L);
    }

    @Test
    void eliminarTomaInventarioEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(tomaInventarioService).eliminarTomaInventario(1L);

        mockMvc.perform(delete("/api/v1/tomas-inventario/1"))
                .andExpect(status().isConflict());
    }
}
