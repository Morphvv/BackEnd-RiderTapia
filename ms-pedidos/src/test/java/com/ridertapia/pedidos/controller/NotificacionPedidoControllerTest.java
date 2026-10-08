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

import com.ridertapia.pedidos.model.NotificacionPedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.service.NotificacionPedidoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(NotificacionPedidoController.class)
@ActiveProfiles("test")
class NotificacionPedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacionPedidoService notificacionPedidoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private NotificacionPedido notificacionPedido;

    @BeforeEach
    void setUp() {
        notificacionPedido = new NotificacionPedido(1L, "CONFIRMACION_COMPRA", "ana@correo.cl", LocalDateTime.of(2026, 10, 6, 10, 0), true, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    @Test
    void listarNotificacionesPedido() throws Exception {
        NotificacionPedido otro = new NotificacionPedido(2L, "LISTO_PARA_RETIRO", "ana@correo.cl", LocalDateTime.of(2026, 10, 6, 12, 0), false, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(notificacionPedidoService.listarNotificacionesPedido()).thenReturn(Arrays.asList(notificacionPedido, otro));

        mockMvc.perform(get("/api/v1/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notificacionPedidoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.notificacionPedidoList[0].tipo").value("CONFIRMACION_COMPRA"))
                .andExpect(jsonPath("$._embedded.notificacionPedidoList[1].tipo").value("LISTO_PARA_RETIRO"))
                .andExpect(jsonPath("$._embedded.notificacionPedidoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(notificacionPedidoService.findById(1L)).thenReturn(Optional.of(notificacionPedido));

        mockMvc.perform(get("/api/v1/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idNotificacionPedido").value(1))
                .andExpect(jsonPath("$.tipo").value("CONFIRMACION_COMPRA"))
                .andExpect(jsonPath("$.destinatario").value("ana@correo.cl"))
                .andExpect(jsonPath("$.fechaEnvio").exists())
                .andExpect(jsonPath("$.enviada").value(true))
                .andExpect(jsonPath("$.pedido.idPedido").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.notificaciones.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(notificacionPedidoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/notificaciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorPedidoConResultados() throws Exception {
        when(notificacionPedidoService.findByPedido(1L)).thenReturn(List.of(notificacionPedido));

        mockMvc.perform(get("/api/v1/notificaciones/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notificacionPedidoList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.notificacionPedidoList[0].idNotificacionPedido").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorPedidoSinResultados() throws Exception {
        when(notificacionPedidoService.findByPedido(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notificaciones/pedido/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearNotificacionPedido() throws Exception {
        when(notificacionPedidoService.guardarNotificacionPedido(any(NotificacionPedido.class))).thenReturn(notificacionPedido);

        mockMvc.perform(post("/api/v1/notificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificacionPedido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idNotificacionPedido").value(1))
                .andExpect(jsonPath("$.tipo").value("CONFIRMACION_COMPRA"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.notificaciones.href").exists());

        verify(notificacionPedidoService, times(1)).guardarNotificacionPedido(any(NotificacionPedido.class));
    }

    @Test
    void crearNotificacionPedidoConRelacionNoExistente() throws Exception {
        when(notificacionPedidoService.guardarNotificacionPedido(any(NotificacionPedido.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/notificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificacionPedido)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarNotificacionPedidoExistente() throws Exception {
        NotificacionPedido actualizado = new NotificacionPedido(1L, "PEDIDO_RETIRADO", "ana@correo.cl", LocalDateTime.of(2026, 10, 7, 9, 0), true, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(notificacionPedidoService.actualizarNotificacionPedido(eq(1L), any(NotificacionPedido.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/notificaciones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("PEDIDO_RETIRADO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.notificaciones.href").exists());
    }

    @Test
    void actualizarNotificacionPedidoNoExistente() throws Exception {
        when(notificacionPedidoService.actualizarNotificacionPedido(eq(99L), any(NotificacionPedido.class)))
                .thenThrow(new RuntimeException("NotificacionPedido no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/notificaciones/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificacionPedido)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarNotificacionPedido() throws Exception {
        doNothing().when(notificacionPedidoService).eliminarNotificacionPedido(1L);

        mockMvc.perform(delete("/api/v1/notificaciones/1"))
                .andExpect(status().isNoContent());

        verify(notificacionPedidoService, times(1)).eliminarNotificacionPedido(1L);
    }
}
