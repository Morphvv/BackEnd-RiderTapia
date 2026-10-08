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

import com.ridertapia.inventario.model.ItemInventario;

import com.ridertapia.inventario.service.ItemInventarioService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(ItemInventarioController.class)
@ActiveProfiles("test")
class ItemInventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemInventarioService itemInventarioService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ItemInventario itemInventario;

    @BeforeEach
    void setUp() {
        itemInventario = new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)));
    }

    @Test
    void listarItemsInventario() throws Exception {
        ItemInventario otro = new ItemInventario(2L, 11L, 0, 0, 3, 45000, LocalDateTime.of(2026, 10, 6, 12, 0), new ArrayList<>(List.of(3L)));
        when(itemInventarioService.listarItemsInventario()).thenReturn(Arrays.asList(itemInventario, otro));

        mockMvc.perform(get("/api/v1/items-inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.itemInventarioList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.itemInventarioList[0].idVariante").value(10))
                .andExpect(jsonPath("$._embedded.itemInventarioList[1].idVariante").value(11))
                .andExpect(jsonPath("$._embedded.itemInventarioList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(itemInventarioService.findById(1L)).thenReturn(Optional.of(itemInventario));

        mockMvc.perform(get("/api/v1/items-inventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItemInventario").value(1))
                .andExpect(jsonPath("$.idVariante").value(10))
                .andExpect(jsonPath("$.stockDisponible").value(15))
                .andExpect(jsonPath("$.stockReservado").value(2))
                .andExpect(jsonPath("$.stockMinimo").value(3))
                .andExpect(jsonPath("$.costoPromedio").value(650000))
                .andExpect(jsonPath("$.fechaActualizacion").exists())
                .andExpect(jsonPath("$.idProveedores").isArray())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.itemsInventario.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(itemInventarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/items-inventario/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorIdVarianteExistente() throws Exception {
        when(itemInventarioService.findByIdVariante(10L)).thenReturn(itemInventario);

        mockMvc.perform(get("/api/v1/items-inventario/idVariante/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItemInventario").value(1))
                .andExpect(jsonPath("$.idVariante").value(10L))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.itemsInventario.href").exists());
    }

    @Test
    void obtenerPorIdVarianteNoExistente() throws Exception {
        when(itemInventarioService.findByIdVariante(99L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/items-inventario/idVariante/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearItemInventario() throws Exception {
        when(itemInventarioService.guardarItemInventario(any(ItemInventario.class))).thenReturn(itemInventario);

        mockMvc.perform(post("/api/v1/items-inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemInventario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idItemInventario").value(1))
                .andExpect(jsonPath("$.stockDisponible").value(15))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.itemsInventario.href").exists());

        verify(itemInventarioService, times(1)).guardarItemInventario(any(ItemInventario.class));
    }

    @Test
    void crearItemInventarioConRelacionNoExistente() throws Exception {
        when(itemInventarioService.guardarItemInventario(any(ItemInventario.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/items-inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemInventario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearItemInventarioDuplicado() throws Exception {
        when(itemInventarioService.guardarItemInventario(any(ItemInventario.class)))
                .thenThrow(new DataIntegrityViolationException("valor duplicado"));

        mockMvc.perform(post("/api/v1/items-inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemInventario)))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarItemInventarioExistente() throws Exception {
        ItemInventario actualizado = new ItemInventario(1L, 12L, 12, 5, 4, 640000, LocalDateTime.of(2026, 10, 7, 9, 0), new ArrayList<>(List.of(1L)));
        when(itemInventarioService.actualizarItemInventario(eq(1L), any(ItemInventario.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/items-inventario/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockDisponible").value(12))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.itemsInventario.href").exists());
    }

    @Test
    void actualizarItemInventarioNoExistente() throws Exception {
        when(itemInventarioService.actualizarItemInventario(eq(99L), any(ItemInventario.class)))
                .thenThrow(new RuntimeException("ItemInventario no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/items-inventario/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemInventario)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarItemInventario() throws Exception {
        doNothing().when(itemInventarioService).eliminarItemInventario(1L);

        mockMvc.perform(delete("/api/v1/items-inventario/1"))
                .andExpect(status().isNoContent());

        verify(itemInventarioService, times(1)).eliminarItemInventario(1L);
    }

    @Test
    void eliminarItemInventarioEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(itemInventarioService).eliminarItemInventario(1L);

        mockMvc.perform(delete("/api/v1/items-inventario/1"))
                .andExpect(status().isConflict());
    }
}
