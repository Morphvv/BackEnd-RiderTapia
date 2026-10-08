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

import com.ridertapia.productos.model.AvisoStock;
import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.service.AvisoStockService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AvisoStockController.class)
@ActiveProfiles("test")
class AvisoStockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvisoStockService avisoStockService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private AvisoStock avisoStock;

    @BeforeEach
    void setUp() {
        avisoStock = new AvisoStock(1L, "cliente@correo.cl", LocalDateTime.of(2026, 10, 6, 10, 0), false, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void listarAvisosStock() throws Exception {
        AvisoStock otro = new AvisoStock(2L, "otro@correo.cl", LocalDateTime.of(2026, 10, 6, 12, 0), false, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(avisoStockService.listarAvisosStock()).thenReturn(Arrays.asList(avisoStock, otro));

        mockMvc.perform(get("/api/v1/avisos-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.avisoStockList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.avisoStockList[0].correo").value("cliente@correo.cl"))
                .andExpect(jsonPath("$._embedded.avisoStockList[1].correo").value("otro@correo.cl"))
                .andExpect(jsonPath("$._embedded.avisoStockList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(avisoStockService.findById(1L)).thenReturn(Optional.of(avisoStock));

        mockMvc.perform(get("/api/v1/avisos-stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAvisoStock").value(1))
                .andExpect(jsonPath("$.correo").value("cliente@correo.cl"))
                .andExpect(jsonPath("$.fechaSolicitud").exists())
                .andExpect(jsonPath("$.notificado").value(false))
                .andExpect(jsonPath("$.producto.idProducto").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.avisosStock.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(avisoStockService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/avisos-stock/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorProductoConResultados() throws Exception {
        when(avisoStockService.findByProducto(1L)).thenReturn(List.of(avisoStock));

        mockMvc.perform(get("/api/v1/avisos-stock/producto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.avisoStockList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.avisoStockList[0].idAvisoStock").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorProductoSinResultados() throws Exception {
        when(avisoStockService.findByProducto(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/avisos-stock/producto/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearAvisoStock() throws Exception {
        when(avisoStockService.guardarAvisoStock(any(AvisoStock.class))).thenReturn(avisoStock);

        mockMvc.perform(post("/api/v1/avisos-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avisoStock)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAvisoStock").value(1))
                .andExpect(jsonPath("$.correo").value("cliente@correo.cl"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.avisosStock.href").exists());

        verify(avisoStockService, times(1)).guardarAvisoStock(any(AvisoStock.class));
    }

    @Test
    void crearAvisoStockConRelacionNoExistente() throws Exception {
        when(avisoStockService.guardarAvisoStock(any(AvisoStock.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/avisos-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avisoStock)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarAvisoStockExistente() throws Exception {
        AvisoStock actualizado = new AvisoStock(1L, "nuevo@correo.cl", LocalDateTime.of(2026, 10, 7, 9, 0), true, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(avisoStockService.actualizarAvisoStock(eq(1L), any(AvisoStock.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/avisos-stock/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("nuevo@correo.cl"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.avisosStock.href").exists());
    }

    @Test
    void actualizarAvisoStockNoExistente() throws Exception {
        when(avisoStockService.actualizarAvisoStock(eq(99L), any(AvisoStock.class)))
                .thenThrow(new RuntimeException("AvisoStock no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/avisos-stock/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(avisoStock)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarAvisoStock() throws Exception {
        doNothing().when(avisoStockService).eliminarAvisoStock(1L);

        mockMvc.perform(delete("/api/v1/avisos-stock/1"))
                .andExpect(status().isNoContent());

        verify(avisoStockService, times(1)).eliminarAvisoStock(1L);
    }
}
