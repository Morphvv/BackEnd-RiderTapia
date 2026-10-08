package com.ridertapia.productos.controller;

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

import com.ridertapia.productos.model.Compatibilidad;

import com.ridertapia.productos.service.CompatibilidadService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(CompatibilidadController.class)
@ActiveProfiles("test")
class CompatibilidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompatibilidadService compatibilidadService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Compatibilidad compatibilidad;

    @BeforeEach
    void setUp() {
        compatibilidad = new Compatibilidad(1L, "MTB", "29", "12");
    }

    @Test
    void listarCompatibilidades() throws Exception {
        Compatibilidad otro = new Compatibilidad(2L, "Ruta", "700c", "11");
        when(compatibilidadService.listarCompatibilidades()).thenReturn(Arrays.asList(compatibilidad, otro));

        mockMvc.perform(get("/api/v1/compatibilidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.compatibilidadList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.compatibilidadList[0].tipoBicicleta").value("MTB"))
                .andExpect(jsonPath("$._embedded.compatibilidadList[1].tipoBicicleta").value("Ruta"))
                .andExpect(jsonPath("$._embedded.compatibilidadList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(compatibilidadService.findById(1L)).thenReturn(Optional.of(compatibilidad));

        mockMvc.perform(get("/api/v1/compatibilidades/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCompatibilidad").value(1))
                .andExpect(jsonPath("$.tipoBicicleta").value("MTB"))
                .andExpect(jsonPath("$.aro").value("29"))
                .andExpect(jsonPath("$.velocidades").value("12"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.compatibilidades.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(compatibilidadService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/compatibilidades/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorTipoBicicletaConResultados() throws Exception {
        when(compatibilidadService.findByTipoBicicleta("MTB")).thenReturn(List.of(compatibilidad));

        mockMvc.perform(get("/api/v1/compatibilidades/tipoBicicleta/MTB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.compatibilidadList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.compatibilidadList[0].idCompatibilidad").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorTipoBicicletaSinResultados() throws Exception {
        when(compatibilidadService.findByTipoBicicleta("BMX")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/compatibilidades/tipoBicicleta/BMX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearCompatibilidad() throws Exception {
        when(compatibilidadService.guardarCompatibilidad(any(Compatibilidad.class))).thenReturn(compatibilidad);

        mockMvc.perform(post("/api/v1/compatibilidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compatibilidad)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCompatibilidad").value(1))
                .andExpect(jsonPath("$.aro").value("29"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.compatibilidades.href").exists());

        verify(compatibilidadService, times(1)).guardarCompatibilidad(any(Compatibilidad.class));
    }

    @Test
    void actualizarCompatibilidadExistente() throws Exception {
        Compatibilidad actualizado = new Compatibilidad(1L, "MTB", "27.5", "10");
        when(compatibilidadService.actualizarCompatibilidad(eq(1L), any(Compatibilidad.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/compatibilidades/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aro").value("27.5"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.compatibilidades.href").exists());
    }

    @Test
    void actualizarCompatibilidadNoExistente() throws Exception {
        when(compatibilidadService.actualizarCompatibilidad(eq(99L), any(Compatibilidad.class)))
                .thenThrow(new RuntimeException("Compatibilidad no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/compatibilidades/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(compatibilidad)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarCompatibilidad() throws Exception {
        doNothing().when(compatibilidadService).eliminarCompatibilidad(1L);

        mockMvc.perform(delete("/api/v1/compatibilidades/1"))
                .andExpect(status().isNoContent());

        verify(compatibilidadService, times(1)).eliminarCompatibilidad(1L);
    }

    @Test
    void eliminarCompatibilidadEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(compatibilidadService).eliminarCompatibilidad(1L);

        mockMvc.perform(delete("/api/v1/compatibilidades/1"))
                .andExpect(status().isConflict());
    }
}
