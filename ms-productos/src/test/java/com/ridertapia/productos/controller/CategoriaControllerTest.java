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

import com.ridertapia.productos.model.Categoria;

import com.ridertapia.productos.service.CategoriaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(CategoriaController.class)
@ActiveProfiles("test")
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null);
    }

    @Test
    void listarCategorias() throws Exception {
        Categoria otro = new Categoria(2L, "Accesorios", "Cascos, luces y repuestos", true, null);
        when(categoriaService.listarCategorias()).thenReturn(Arrays.asList(categoria, otro));

        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.categoriaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.categoriaList[0].nombre").value("Bicicletas"))
                .andExpect(jsonPath("$._embedded.categoriaList[1].nombre").value("Accesorios"))
                .andExpect(jsonPath("$._embedded.categoriaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(categoriaService.findById(1L)).thenReturn(Optional.of(categoria));

        mockMvc.perform(get("/api/v1/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(1))
                .andExpect(jsonPath("$.nombre").value("Bicicletas"))
                .andExpect(jsonPath("$.descripcion").value("Bicicletas de todo tipo"))
                .andExpect(jsonPath("$.activa").value(true))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.categorias.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(categoriaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/categorias/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorNombreExistente() throws Exception {
        when(categoriaService.findByNombre("Bicicletas")).thenReturn(categoria);

        mockMvc.perform(get("/api/v1/categorias/nombre/Bicicletas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(1))
                .andExpect(jsonPath("$.nombre").value("Bicicletas"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.categorias.href").exists());
    }

    @Test
    void obtenerPorNombreNoExistente() throws Exception {
        when(categoriaService.findByNombre("NO-EXISTE")).thenReturn(null);

        mockMvc.perform(get("/api/v1/categorias/nombre/NO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearCategoria() throws Exception {
        when(categoriaService.guardarCategoria(any(Categoria.class))).thenReturn(categoria);

        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").value(1))
                .andExpect(jsonPath("$.nombre").value("Bicicletas"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.categorias.href").exists());

        verify(categoriaService, times(1)).guardarCategoria(any(Categoria.class));
    }

    @Test
    void crearCategoriaConRelacionNoExistente() throws Exception {
        when(categoriaService.guardarCategoria(any(Categoria.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearCategoriaDuplicado() throws Exception {
        when(categoriaService.guardarCategoria(any(Categoria.class)))
                .thenThrow(new DataIntegrityViolationException("valor duplicado"));

        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarCategoriaExistente() throws Exception {
        Categoria actualizado = new Categoria(1L, "Bicicletas MTB", "Bicicletas de montaña", false, null);
        when(categoriaService.actualizarCategoria(eq(1L), any(Categoria.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Bicicletas MTB"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.categorias.href").exists());
    }

    @Test
    void actualizarCategoriaNoExistente() throws Exception {
        when(categoriaService.actualizarCategoria(eq(99L), any(Categoria.class)))
                .thenThrow(new RuntimeException("Categoria no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/categorias/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoria)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarCategoria() throws Exception {
        doNothing().when(categoriaService).eliminarCategoria(1L);

        mockMvc.perform(delete("/api/v1/categorias/1"))
                .andExpect(status().isNoContent());

        verify(categoriaService, times(1)).eliminarCategoria(1L);
    }

    @Test
    void eliminarCategoriaEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(categoriaService).eliminarCategoria(1L);

        mockMvc.perform(delete("/api/v1/categorias/1"))
                .andExpect(status().isConflict());
    }
}
