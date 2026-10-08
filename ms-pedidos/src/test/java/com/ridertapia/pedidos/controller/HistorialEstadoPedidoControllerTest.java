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

import com.ridertapia.pedidos.model.HistorialEstadoPedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.service.HistorialEstadoPedidoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(HistorialEstadoPedidoController.class)
@ActiveProfiles("test")
class HistorialEstadoPedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistorialEstadoPedidoService historialEstadoPedidoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private HistorialEstadoPedido historialEstadoPedido;

    @BeforeEach
    void setUp() {
        historialEstadoPedido = new HistorialEstadoPedido(1L, EstadoPedido.PENDIENTE_PAGO, EstadoPedido.PAGADO, LocalDateTime.of(2026, 10, 6, 10, 0), 1L, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    @Test
    void listarHistorialesEstadoPedido() throws Exception {
        HistorialEstadoPedido otro = new HistorialEstadoPedido(2L, EstadoPedido.PAGADO, EstadoPedido.EN_PREPARACION, LocalDateTime.of(2026, 10, 6, 12, 0), 2L, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(historialEstadoPedidoService.listarHistorialesEstadoPedido()).thenReturn(Arrays.asList(historialEstadoPedido, otro));

        mockMvc.perform(get("/api/v1/historial-estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.historialEstadoPedidoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.historialEstadoPedidoList[0].estadoAnterior").value("PENDIENTE_PAGO"))
                .andExpect(jsonPath("$._embedded.historialEstadoPedidoList[1].estadoAnterior").value("PAGADO"))
                .andExpect(jsonPath("$._embedded.historialEstadoPedidoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(historialEstadoPedidoService.findById(1L)).thenReturn(Optional.of(historialEstadoPedido));

        mockMvc.perform(get("/api/v1/historial-estados/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHistorialEstadoPedido").value(1))
                .andExpect(jsonPath("$.estadoAnterior").value("PENDIENTE_PAGO"))
                .andExpect(jsonPath("$.estadoNuevo").value("PAGADO"))
                .andExpect(jsonPath("$.fechaCambio").exists())
                .andExpect(jsonPath("$.idEmpleado").value(1))
                .andExpect(jsonPath("$.pedido.idPedido").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.historialEstados.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(historialEstadoPedidoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/historial-estados/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorPedidoConResultados() throws Exception {
        when(historialEstadoPedidoService.findByPedido(1L)).thenReturn(List.of(historialEstadoPedido));

        mockMvc.perform(get("/api/v1/historial-estados/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.historialEstadoPedidoList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.historialEstadoPedidoList[0].idHistorialEstadoPedido").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorPedidoSinResultados() throws Exception {
        when(historialEstadoPedidoService.findByPedido(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/historial-estados/pedido/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearHistorialEstadoPedido() throws Exception {
        when(historialEstadoPedidoService.guardarHistorialEstadoPedido(any(HistorialEstadoPedido.class))).thenReturn(historialEstadoPedido);

        mockMvc.perform(post("/api/v1/historial-estados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(historialEstadoPedido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idHistorialEstadoPedido").value(1))
                .andExpect(jsonPath("$.estadoNuevo").value("PAGADO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.historialEstados.href").exists());

        verify(historialEstadoPedidoService, times(1)).guardarHistorialEstadoPedido(any(HistorialEstadoPedido.class));
    }

    @Test
    void crearHistorialEstadoPedidoConRelacionNoExistente() throws Exception {
        when(historialEstadoPedidoService.guardarHistorialEstadoPedido(any(HistorialEstadoPedido.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/historial-estados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(historialEstadoPedido)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarHistorialEstadoPedidoExistente() throws Exception {
        HistorialEstadoPedido actualizado = new HistorialEstadoPedido(1L, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO_PARA_RETIRO, LocalDateTime.of(2026, 10, 7, 9, 0), 3L, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(historialEstadoPedidoService.actualizarHistorialEstadoPedido(eq(1L), any(HistorialEstadoPedido.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/historial-estados/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estadoNuevo").value("LISTO_PARA_RETIRO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.historialEstados.href").exists());
    }

    @Test
    void actualizarHistorialEstadoPedidoNoExistente() throws Exception {
        when(historialEstadoPedidoService.actualizarHistorialEstadoPedido(eq(99L), any(HistorialEstadoPedido.class)))
                .thenThrow(new RuntimeException("HistorialEstadoPedido no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/historial-estados/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(historialEstadoPedido)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarHistorialEstadoPedido() throws Exception {
        doNothing().when(historialEstadoPedidoService).eliminarHistorialEstadoPedido(1L);

        mockMvc.perform(delete("/api/v1/historial-estados/1"))
                .andExpect(status().isNoContent());

        verify(historialEstadoPedidoService, times(1)).eliminarHistorialEstadoPedido(1L);
    }
}
