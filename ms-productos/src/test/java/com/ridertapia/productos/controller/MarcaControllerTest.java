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

import com.ridertapia.productos.model.Marca;

import com.ridertapia.productos.service.MarcaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(MarcaController.class)
@ActiveProfiles("test")
class MarcaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MarcaService marcaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Marca marca;

    @BeforeEach
    void setUp() {
        marca = new Marca(1L, "Trek", true);
    }

    @Test
    void listarMarcas() throws Exception {
        Marca otro = new Marca(2L, "Specialized", true);
        when(marcaService.listarMarcas()).thenReturn(Arrays.asList(marca, otro));

        mockMvc.perform(get("/api/v1/marcas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.marcaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.marcaList[0].nombre").value("Trek"))
                .andExpect(jsonPath("$._embedded.marcaList[1].nombre").value("Specialized"))
                .andExpect(jsonPath("$._embedded.marcaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(marcaService.findById(1L)).thenReturn(Optional.of(marca));

        mockMvc.perform(get("/api/v1/marcas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMarca").value(1))
                .andExpect(jsonPath("$.nombre").value("Trek"))
                .andExpect(jsonPath("$.activa").value(true))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.marcas.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(marcaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/marcas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorNombreExistente() throws Exception {
        when(marcaService.findByNombre("Trek")).thenReturn(marca);

        mockMvc.perform(get("/api/v1/marcas/nombre/Trek"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMarca").value(1))
                .andExpect(jsonPath("$.nombre").value("Trek"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.marcas.href").exists());
    }

    @Test
    void obtenerPorNombreNoExistente() throws Exception {
        when(marcaService.findByNombre("NO-EXISTE")).thenReturn(null);

        mockMvc.perform(get("/api/v1/marcas/nombre/NO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearMarca() throws Exception {
        when(marcaService.guardarMarca(any(Marca.class))).thenReturn(marca);

        mockMvc.perform(post("/api/v1/marcas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(marca)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMarca").value(1))
                .andExpect(jsonPath("$.nombre").value("Trek"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.marcas.href").exists());

        verify(marcaService, times(1)).guardarMarca(any(Marca.class));
    }

    @Test
    void crearMarcaDuplicado() throws Exception {
        when(marcaService.guardarMarca(any(Marca.class)))
                .thenThrow(new DataIntegrityViolationException("valor duplicado"));

        mockMvc.perform(post("/api/v1/marcas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(marca)))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarMarcaExistente() throws Exception {
        Marca actualizado = new Marca(1L, "Trek Bikes", false);
        when(marcaService.actualizarMarca(eq(1L), any(Marca.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/marcas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Trek Bikes"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.marcas.href").exists());
    }

    @Test
    void actualizarMarcaNoExistente() throws Exception {
        when(marcaService.actualizarMarca(eq(99L), any(Marca.class)))
                .thenThrow(new RuntimeException("Marca no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/marcas/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(marca)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarMarca() throws Exception {
        doNothing().when(marcaService).eliminarMarca(1L);

        mockMvc.perform(delete("/api/v1/marcas/1"))
                .andExpect(status().isNoContent());

        verify(marcaService, times(1)).eliminarMarca(1L);
    }

    @Test
    void eliminarMarcaEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(marcaService).eliminarMarca(1L);

        mockMvc.perform(delete("/api/v1/marcas/1"))
                .andExpect(status().isConflict());
    }
}
