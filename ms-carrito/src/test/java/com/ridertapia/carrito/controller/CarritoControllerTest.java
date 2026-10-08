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
import com.ridertapia.carrito.model.EstadoCarrito;
import com.ridertapia.carrito.service.CarritoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(CarritoController.class)
@ActiveProfiles("test")
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Carrito carrito;

    @BeforeEach
    void setUp() {
        carrito = new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L);
    }

    @Test
    void listarCarritos() throws Exception {
        Carrito otro = new Carrito(2L, "11111111-1", "tok-def456", LocalDateTime.of(2026, 10, 6, 12, 0), LocalDateTime.of(2026, 10, 6, 12, 30), EstadoCarrito.ACTIVO, 79990, 0, 79990, 3L);
        when(carritoService.listarCarritos()).thenReturn(Arrays.asList(carrito, otro));

        mockMvc.perform(get("/api/v1/carritos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.carritoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.carritoList[0].rutCliente").value("12345678-5"))
                .andExpect(jsonPath("$._embedded.carritoList[1].rutCliente").value("11111111-1"))
                .andExpect(jsonPath("$._embedded.carritoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(carritoService.findById(1L)).thenReturn(Optional.of(carrito));

        mockMvc.perform(get("/api/v1/carritos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito").value(1))
                .andExpect(jsonPath("$.rutCliente").value("12345678-5"))
                .andExpect(jsonPath("$.tokenSesion").value("tok-abc123"))
                .andExpect(jsonPath("$.fechaCreacion").exists())
                .andExpect(jsonPath("$.fechaActualizacion").exists())
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.subtotal").value(899990))
                .andExpect(jsonPath("$.descuento").value(0))
                .andExpect(jsonPath("$.total").value(899990))
                .andExpect(jsonPath("$.idCupon").value(2))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.carritos.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(carritoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/carritos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorRutClienteConResultados() throws Exception {
        when(carritoService.findByRutCliente("12345678-5")).thenReturn(List.of(carrito));

        mockMvc.perform(get("/api/v1/carritos/rutCliente/12345678-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.carritoList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.carritoList[0].idCarrito").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorRutClienteSinResultados() throws Exception {
        when(carritoService.findByRutCliente("99999999-9")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/carritos/rutCliente/99999999-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearCarrito() throws Exception {
        when(carritoService.guardarCarrito(any(Carrito.class))).thenReturn(carrito);

        mockMvc.perform(post("/api/v1/carritos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carrito)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCarrito").value(1))
                .andExpect(jsonPath("$.total").value(899990))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.carritos.href").exists());

        verify(carritoService, times(1)).guardarCarrito(any(Carrito.class));
    }

    @Test
    void crearCarritoConClienteNoExistente() throws Exception {
        when(carritoService.guardarCarrito(any(Carrito.class)))
                .thenThrow(new RuntimeException("Cliente no encontrado con rut: 12345678-5"));

        mockMvc.perform(post("/api/v1/carritos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carrito)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarCarritoExistente() throws Exception {
        Carrito actualizado = new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 7, 9, 0), LocalDateTime.of(2026, 10, 7, 9, 30), EstadoCarrito.CONVERTIDO_EN_PEDIDO, 899990, 89999, 809991, 2L);
        when(carritoService.actualizarCarrito(eq(1L), any(Carrito.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/carritos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(809991))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.carritos.href").exists());
    }

    @Test
    void actualizarCarritoNoExistente() throws Exception {
        when(carritoService.actualizarCarrito(eq(99L), any(Carrito.class)))
                .thenThrow(new RuntimeException("Carrito no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/carritos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(carrito)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarCarrito() throws Exception {
        doNothing().when(carritoService).eliminarCarrito(1L);

        mockMvc.perform(delete("/api/v1/carritos/1"))
                .andExpect(status().isNoContent());

        verify(carritoService, times(1)).eliminarCarrito(1L);
    }

    @Test
    void eliminarCarritoEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(carritoService).eliminarCarrito(1L);

        mockMvc.perform(delete("/api/v1/carritos/1"))
                .andExpect(status().isConflict());
    }
}
