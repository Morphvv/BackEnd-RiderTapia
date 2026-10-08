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

import com.ridertapia.inventario.model.CargaMasiva;

import com.ridertapia.inventario.service.CargaMasivaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(CargaMasivaController.class)
@ActiveProfiles("test")
class CargaMasivaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CargaMasivaService cargaMasivaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private CargaMasiva cargaMasiva;

    @BeforeEach
    void setUp() {
        cargaMasiva = new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L);
    }

    @Test
    void listarCargasMasivas() throws Exception {
        CargaMasiva otro = new CargaMasiva(2L, "repuestos.xlsx", LocalDateTime.of(2026, 10, 6, 12, 0), 40, 0, 2L);
        when(cargaMasivaService.listarCargasMasivas()).thenReturn(Arrays.asList(cargaMasiva, otro));

        mockMvc.perform(get("/api/v1/cargas-masivas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cargaMasivaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.cargaMasivaList[0].nombreArchivo").value("productos_octubre.xlsx"))
                .andExpect(jsonPath("$._embedded.cargaMasivaList[1].nombreArchivo").value("repuestos.xlsx"))
                .andExpect(jsonPath("$._embedded.cargaMasivaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(cargaMasivaService.findById(1L)).thenReturn(Optional.of(cargaMasiva));

        mockMvc.perform(get("/api/v1/cargas-masivas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCargaMasiva").value(1))
                .andExpect(jsonPath("$.nombreArchivo").value("productos_octubre.xlsx"))
                .andExpect(jsonPath("$.fechaCarga").exists())
                .andExpect(jsonPath("$.filasProcesadas").value(120))
                .andExpect(jsonPath("$.filasConError").value(2))
                .andExpect(jsonPath("$.idEmpleado").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cargasMasivas.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(cargaMasivaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/cargas-masivas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorIdEmpleadoConResultados() throws Exception {
        when(cargaMasivaService.findByIdEmpleado(1L)).thenReturn(List.of(cargaMasiva));

        mockMvc.perform(get("/api/v1/cargas-masivas/idEmpleado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cargaMasivaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.cargaMasivaList[0].idCargaMasiva").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdEmpleadoSinResultados() throws Exception {
        when(cargaMasivaService.findByIdEmpleado(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cargas-masivas/idEmpleado/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearCargaMasiva() throws Exception {
        when(cargaMasivaService.guardarCargaMasiva(any(CargaMasiva.class))).thenReturn(cargaMasiva);

        mockMvc.perform(post("/api/v1/cargas-masivas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cargaMasiva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCargaMasiva").value(1))
                .andExpect(jsonPath("$.filasProcesadas").value(120))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cargasMasivas.href").exists());

        verify(cargaMasivaService, times(1)).guardarCargaMasiva(any(CargaMasiva.class));
    }

    @Test
    void actualizarCargaMasivaExistente() throws Exception {
        CargaMasiva actualizado = new CargaMasiva(1L, "productos_octubre_v2.xlsx", LocalDateTime.of(2026, 10, 7, 9, 0), 118, 1, 1L);
        when(cargaMasivaService.actualizarCargaMasiva(eq(1L), any(CargaMasiva.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/cargas-masivas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filasProcesadas").value(118))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cargasMasivas.href").exists());
    }

    @Test
    void actualizarCargaMasivaNoExistente() throws Exception {
        when(cargaMasivaService.actualizarCargaMasiva(eq(99L), any(CargaMasiva.class)))
                .thenThrow(new RuntimeException("CargaMasiva no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/cargas-masivas/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cargaMasiva)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarCargaMasiva() throws Exception {
        doNothing().when(cargaMasivaService).eliminarCargaMasiva(1L);

        mockMvc.perform(delete("/api/v1/cargas-masivas/1"))
                .andExpect(status().isNoContent());

        verify(cargaMasivaService, times(1)).eliminarCargaMasiva(1L);
    }

    @Test
    void eliminarCargaMasivaEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(cargaMasivaService).eliminarCargaMasiva(1L);

        mockMvc.perform(delete("/api/v1/cargas-masivas/1"))
                .andExpect(status().isConflict());
    }
}
