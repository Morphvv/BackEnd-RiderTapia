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

import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.Resena;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.service.ResenaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(ResenaController.class)
@ActiveProfiles("test")
class ResenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResenaService resenaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Resena resena;

    @BeforeEach
    void setUp() {
        resena = new Resena(1L, 3L, 5, "Excelente bicicleta", LocalDateTime.of(2026, 10, 6, 10, 0), true, "Gracias por tu compra", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void listarResenas() throws Exception {
        Resena otro = new Resena(2L, 4L, 4, "Buena relación precio calidad", LocalDateTime.of(2026, 10, 6, 12, 0), true, "Gracias por comentar", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(resenaService.listarResenas()).thenReturn(Arrays.asList(resena, otro));

        mockMvc.perform(get("/api/v1/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.resenaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.resenaList[0].idCliente").value(3))
                .andExpect(jsonPath("$._embedded.resenaList[1].idCliente").value(4))
                .andExpect(jsonPath("$._embedded.resenaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(resenaService.findById(1L)).thenReturn(Optional.of(resena));

        mockMvc.perform(get("/api/v1/resenas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idResena").value(1))
                .andExpect(jsonPath("$.idCliente").value(3))
                .andExpect(jsonPath("$.calificacion").value(5))
                .andExpect(jsonPath("$.comentario").value("Excelente bicicleta"))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.visible").value(true))
                .andExpect(jsonPath("$.respuestaTienda").value("Gracias por tu compra"))
                .andExpect(jsonPath("$.producto.idProducto").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.resenas.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(resenaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/resenas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorProductoConResultados() throws Exception {
        when(resenaService.findByProducto(1L)).thenReturn(List.of(resena));

        mockMvc.perform(get("/api/v1/resenas/producto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.resenaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.resenaList[0].idResena").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorProductoSinResultados() throws Exception {
        when(resenaService.findByProducto(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/resenas/producto/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearResena() throws Exception {
        when(resenaService.guardarResena(any(Resena.class))).thenReturn(resena);

        mockMvc.perform(post("/api/v1/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resena)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idResena").value(1))
                .andExpect(jsonPath("$.calificacion").value(5))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.resenas.href").exists());

        verify(resenaService, times(1)).guardarResena(any(Resena.class));
    }

    @Test
    void crearResenaConRelacionNoExistente() throws Exception {
        when(resenaService.guardarResena(any(Resena.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resena)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarResenaExistente() throws Exception {
        Resena actualizado = new Resena(1L, 3L, 3, "Cambié de opinión", LocalDateTime.of(2026, 10, 7, 9, 0), false, "Lamentamos tu experiencia", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(resenaService.actualizarResena(eq(1L), any(Resena.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/resenas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calificacion").value(3))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.resenas.href").exists());
    }

    @Test
    void actualizarResenaNoExistente() throws Exception {
        when(resenaService.actualizarResena(eq(99L), any(Resena.class)))
                .thenThrow(new RuntimeException("Resena no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/resenas/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resena)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarResena() throws Exception {
        doNothing().when(resenaService).eliminarResena(1L);

        mockMvc.perform(delete("/api/v1/resenas/1"))
                .andExpect(status().isNoContent());

        verify(resenaService, times(1)).eliminarResena(1L);
    }
}
