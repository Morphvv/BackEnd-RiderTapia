package com.ridertapia.carrito.controller;

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

import com.ridertapia.carrito.model.Carrito;
import com.ridertapia.carrito.model.ItemCarrito;
import com.ridertapia.carrito.model.EstadoCarrito;
import com.ridertapia.carrito.service.ItemCarritoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(ItemCarritoController.class)
@ActiveProfiles("test")
class ItemCarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemCarritoService itemCarritoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ItemCarrito itemCarrito;

    @BeforeEach
    void setUp() {
        itemCarrito = new ItemCarrito(1L, 10L, 1, 899990, LocalDateTime.of(2026, 10, 6, 11, 0), new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L));
    }

    @Test
    void listarItemsCarrito() throws Exception {
        ItemCarrito otro = new ItemCarrito(2L, 11L, 2, 39995, LocalDateTime.of(2026, 10, 6, 12, 0), new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L));
        when(itemCarritoService.listarItemsCarrito()).thenReturn(Arrays.asList(itemCarrito, otro));

        mockMvc.perform(get("/api/v1/items-carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.itemCarritoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.itemCarritoList[0].idVariante").value(10))
                .andExpect(jsonPath("$._embedded.itemCarritoList[1].idVariante").value(11))
                .andExpect(jsonPath("$._embedded.itemCarritoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(itemCarritoService.findById(1L)).thenReturn(Optional.of(itemCarrito));

        mockMvc.perform(get("/api/v1/items-carrito/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItemCarrito").value(1))
                .andExpect(jsonPath("$.idVariante").value(10))
                .andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$.precioUnitario").value(899990))
                .andExpect(jsonPath("$.fechaAgregado").exists())
                .andExpect(jsonPath("$.carrito.idCarrito").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.itemsCarrito.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(itemCarritoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/items-carrito/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorCarritoConResultados() throws Exception {
        when(itemCarritoService.findByCarrito(1L)).thenReturn(List.of(itemCarrito));

        mockMvc.perform(get("/api/v1/items-carrito/carrito/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.itemCarritoList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.itemCarritoList[0].idItemCarrito").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorCarritoSinResultados() throws Exception {
        when(itemCarritoService.findByCarrito(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/items-carrito/carrito/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearItemCarrito() throws Exception {
        when(itemCarritoService.guardarItemCarrito(any(ItemCarrito.class))).thenReturn(itemCarrito);

        mockMvc.perform(post("/api/v1/items-carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemCarrito)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idItemCarrito").value(1))
                .andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.itemsCarrito.href").exists());

        verify(itemCarritoService, times(1)).guardarItemCarrito(any(ItemCarrito.class));
    }

    @Test
    void crearItemCarritoConRelacionNoExistente() throws Exception {
        when(itemCarritoService.guardarItemCarrito(any(ItemCarrito.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/items-carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemCarrito)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarItemCarritoExistente() throws Exception {
        ItemCarrito actualizado = new ItemCarrito(1L, 10L, 3, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L));
        when(itemCarritoService.actualizarItemCarrito(eq(1L), any(ItemCarrito.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/items-carrito/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.itemsCarrito.href").exists());
    }

    @Test
    void actualizarItemCarritoNoExistente() throws Exception {
        when(itemCarritoService.actualizarItemCarrito(eq(99L), any(ItemCarrito.class)))
                .thenThrow(new RuntimeException("ItemCarrito no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/items-carrito/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemCarrito)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarItemCarrito() throws Exception {
        doNothing().when(itemCarritoService).eliminarItemCarrito(1L);

        mockMvc.perform(delete("/api/v1/items-carrito/1"))
                .andExpect(status().isNoContent());

        verify(itemCarritoService, times(1)).eliminarItemCarrito(1L);
    }

    @Test
    void crearItemCarritoSinStock() throws Exception {
        when(itemCarritoService.guardarItemCarrito(any(ItemCarrito.class)))
                .thenThrow(new IllegalStateException("Stock insuficiente para la variante con id: 10"));

        mockMvc.perform(post("/api/v1/items-carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemCarrito)))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarItemCarritoSinStock() throws Exception {
        when(itemCarritoService.actualizarItemCarrito(eq(1L), any(ItemCarrito.class)))
                .thenThrow(new IllegalStateException("Stock insuficiente para la variante con id: 10"));

        mockMvc.perform(put("/api/v1/items-carrito/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemCarrito)))
                .andExpect(status().isConflict());
    }
}
