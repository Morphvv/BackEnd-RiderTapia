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

import com.ridertapia.pedidos.model.DetallePedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.service.DetallePedidoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(DetallePedidoController.class)
@ActiveProfiles("test")
class DetallePedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetallePedidoService detallePedidoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private DetallePedido detallePedido;

    @BeforeEach
    void setUp() {
        detallePedido = new DetallePedido(1L, 10L, 1, 899990, 899990, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    @Test
    void listarDetallesPedido() throws Exception {
        DetallePedido otro = new DetallePedido(2L, 11L, 2, 39995, 79990, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(detallePedidoService.listarDetallesPedido()).thenReturn(Arrays.asList(detallePedido, otro));

        mockMvc.perform(get("/api/v1/detalles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.detallePedidoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.detallePedidoList[0].idVariante").value(10))
                .andExpect(jsonPath("$._embedded.detallePedidoList[1].idVariante").value(11))
                .andExpect(jsonPath("$._embedded.detallePedidoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(detallePedidoService.findById(1L)).thenReturn(Optional.of(detallePedido));

        mockMvc.perform(get("/api/v1/detalles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDetallePedido").value(1))
                .andExpect(jsonPath("$.idVariante").value(10))
                .andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$.precioUnitario").value(899990))
                .andExpect(jsonPath("$.subtotal").value(899990))
                .andExpect(jsonPath("$.pedido.idPedido").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.detalles.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(detallePedidoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/detalles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorPedidoConResultados() throws Exception {
        when(detallePedidoService.findByPedido(1L)).thenReturn(List.of(detallePedido));

        mockMvc.perform(get("/api/v1/detalles/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.detallePedidoList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.detallePedidoList[0].idDetallePedido").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorPedidoSinResultados() throws Exception {
        when(detallePedidoService.findByPedido(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/detalles/pedido/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearDetallePedido() throws Exception {
        when(detallePedidoService.guardarDetallePedido(any(DetallePedido.class))).thenReturn(detallePedido);

        mockMvc.perform(post("/api/v1/detalles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detallePedido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDetallePedido").value(1))
                .andExpect(jsonPath("$.cantidad").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.detalles.href").exists());

        verify(detallePedidoService, times(1)).guardarDetallePedido(any(DetallePedido.class));
    }

    @Test
    void crearDetallePedidoConRelacionNoExistente() throws Exception {
        when(detallePedidoService.guardarDetallePedido(any(DetallePedido.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/detalles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detallePedido)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarDetallePedidoExistente() throws Exception {
        DetallePedido actualizado = new DetallePedido(1L, 10L, 3, 899990, 2699970, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(detallePedidoService.actualizarDetallePedido(eq(1L), any(DetallePedido.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/detalles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.detalles.href").exists());
    }

    @Test
    void actualizarDetallePedidoNoExistente() throws Exception {
        when(detallePedidoService.actualizarDetallePedido(eq(99L), any(DetallePedido.class)))
                .thenThrow(new RuntimeException("DetallePedido no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/detalles/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detallePedido)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarDetallePedido() throws Exception {
        doNothing().when(detallePedidoService).eliminarDetallePedido(1L);

        mockMvc.perform(delete("/api/v1/detalles/1"))
                .andExpect(status().isNoContent());

        verify(detallePedidoService, times(1)).eliminarDetallePedido(1L);
    }
}
