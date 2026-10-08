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

import com.ridertapia.pedidos.model.DatosVisitante;

import com.ridertapia.pedidos.service.DatosVisitanteService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(DatosVisitanteController.class)
@ActiveProfiles("test")
class DatosVisitanteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DatosVisitanteService datosVisitanteService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private DatosVisitante datosVisitante;

    @BeforeEach
    void setUp() {
        datosVisitante = new DatosVisitante(1L, "Ana Soto", "ana@correo.cl", "+56911112222");
    }

    @Test
    void listarDatosVisitantes() throws Exception {
        DatosVisitante otro = new DatosVisitante(2L, "Luis Rojas", "luis@correo.cl", "+56933334444");
        when(datosVisitanteService.listarDatosVisitantes()).thenReturn(Arrays.asList(datosVisitante, otro));

        mockMvc.perform(get("/api/v1/visitantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.datosVisitanteList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.datosVisitanteList[0].nombre").value("Ana Soto"))
                .andExpect(jsonPath("$._embedded.datosVisitanteList[1].nombre").value("Luis Rojas"))
                .andExpect(jsonPath("$._embedded.datosVisitanteList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(datosVisitanteService.findById(1L)).thenReturn(Optional.of(datosVisitante));

        mockMvc.perform(get("/api/v1/visitantes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDatosVisitante").value(1))
                .andExpect(jsonPath("$.nombre").value("Ana Soto"))
                .andExpect(jsonPath("$.correo").value("ana@correo.cl"))
                .andExpect(jsonPath("$.telefono").value("+56911112222"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.visitantes.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(datosVisitanteService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/visitantes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorCorreoConResultados() throws Exception {
        when(datosVisitanteService.findByCorreo("ana@correo.cl")).thenReturn(List.of(datosVisitante));

        mockMvc.perform(get("/api/v1/visitantes/correo/ana@correo.cl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.datosVisitanteList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.datosVisitanteList[0].idDatosVisitante").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorCorreoSinResultados() throws Exception {
        when(datosVisitanteService.findByCorreo("BMX")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/visitantes/correo/BMX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearDatosVisitante() throws Exception {
        when(datosVisitanteService.guardarDatosVisitante(any(DatosVisitante.class))).thenReturn(datosVisitante);

        mockMvc.perform(post("/api/v1/visitantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(datosVisitante)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDatosVisitante").value(1))
                .andExpect(jsonPath("$.nombre").value("Ana Soto"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.visitantes.href").exists());

        verify(datosVisitanteService, times(1)).guardarDatosVisitante(any(DatosVisitante.class));
    }

    @Test
    void actualizarDatosVisitanteExistente() throws Exception {
        DatosVisitante actualizado = new DatosVisitante(1L, "Ana Soto Pérez", "ana.soto@correo.cl", "+56955556666");
        when(datosVisitanteService.actualizarDatosVisitante(eq(1L), any(DatosVisitante.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/visitantes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Soto Pérez"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.visitantes.href").exists());
    }

    @Test
    void actualizarDatosVisitanteNoExistente() throws Exception {
        when(datosVisitanteService.actualizarDatosVisitante(eq(99L), any(DatosVisitante.class)))
                .thenThrow(new RuntimeException("DatosVisitante no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/visitantes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(datosVisitante)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarDatosVisitante() throws Exception {
        doNothing().when(datosVisitanteService).eliminarDatosVisitante(1L);

        mockMvc.perform(delete("/api/v1/visitantes/1"))
                .andExpect(status().isNoContent());

        verify(datosVisitanteService, times(1)).eliminarDatosVisitante(1L);
    }

    @Test
    void eliminarDatosVisitanteEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(datosVisitanteService).eliminarDatosVisitante(1L);

        mockMvc.perform(delete("/api/v1/visitantes/1"))
                .andExpect(status().isConflict());
    }
}
