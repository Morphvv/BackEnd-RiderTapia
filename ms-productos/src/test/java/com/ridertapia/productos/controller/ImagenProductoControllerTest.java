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
import com.ridertapia.productos.model.ImagenProducto;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.service.ImagenProductoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(ImagenProductoController.class)
@ActiveProfiles("test")
class ImagenProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImagenProductoService imagenProductoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ImagenProducto imagenProducto;

    @BeforeEach
    void setUp() {
        imagenProducto = new ImagenProducto(1L, "https://img.ridertapia.cl/mtb29-1.jpg", 1, true, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void listarImagenesProducto() throws Exception {
        ImagenProducto otro = new ImagenProducto(2L, "https://img.ridertapia.cl/mtb29-2.jpg", 2, false, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(imagenProductoService.listarImagenesProducto()).thenReturn(Arrays.asList(imagenProducto, otro));

        mockMvc.perform(get("/api/v1/imagenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.imagenProductoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.imagenProductoList[0].url").value("https://img.ridertapia.cl/mtb29-1.jpg"))
                .andExpect(jsonPath("$._embedded.imagenProductoList[1].url").value("https://img.ridertapia.cl/mtb29-2.jpg"))
                .andExpect(jsonPath("$._embedded.imagenProductoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(imagenProductoService.findById(1L)).thenReturn(Optional.of(imagenProducto));

        mockMvc.perform(get("/api/v1/imagenes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idImagenProducto").value(1))
                .andExpect(jsonPath("$.url").value("https://img.ridertapia.cl/mtb29-1.jpg"))
                .andExpect(jsonPath("$.orden").value(1))
                .andExpect(jsonPath("$.esPrincipal").value(true))
                .andExpect(jsonPath("$.producto.idProducto").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.imagenes.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(imagenProductoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/imagenes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorProductoConResultados() throws Exception {
        when(imagenProductoService.findByProducto(1L)).thenReturn(List.of(imagenProducto));

        mockMvc.perform(get("/api/v1/imagenes/producto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.imagenProductoList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.imagenProductoList[0].idImagenProducto").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorProductoSinResultados() throws Exception {
        when(imagenProductoService.findByProducto(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/imagenes/producto/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearImagenProducto() throws Exception {
        when(imagenProductoService.guardarImagenProducto(any(ImagenProducto.class))).thenReturn(imagenProducto);

        mockMvc.perform(post("/api/v1/imagenes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(imagenProducto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idImagenProducto").value(1))
                .andExpect(jsonPath("$.url").value("https://img.ridertapia.cl/mtb29-1.jpg"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.imagenes.href").exists());

        verify(imagenProductoService, times(1)).guardarImagenProducto(any(ImagenProducto.class));
    }

    @Test
    void crearImagenProductoConRelacionNoExistente() throws Exception {
        when(imagenProductoService.guardarImagenProducto(any(ImagenProducto.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/imagenes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(imagenProducto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarImagenProductoExistente() throws Exception {
        ImagenProducto actualizado = new ImagenProducto(1L, "https://img.ridertapia.cl/mtb29-3.jpg", 3, false, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(imagenProductoService.actualizarImagenProducto(eq(1L), any(ImagenProducto.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/imagenes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://img.ridertapia.cl/mtb29-3.jpg"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.imagenes.href").exists());
    }

    @Test
    void actualizarImagenProductoNoExistente() throws Exception {
        when(imagenProductoService.actualizarImagenProducto(eq(99L), any(ImagenProducto.class)))
                .thenThrow(new RuntimeException("ImagenProducto no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/imagenes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(imagenProducto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarImagenProducto() throws Exception {
        doNothing().when(imagenProductoService).eliminarImagenProducto(1L);

        mockMvc.perform(delete("/api/v1/imagenes/1"))
                .andExpect(status().isNoContent());

        verify(imagenProductoService, times(1)).eliminarImagenProducto(1L);
    }
}
