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

import com.ridertapia.pedidos.model.DatosVisitante;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.service.PedidoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(PedidoController.class)
@ActiveProfiles("test")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null);
    }

    @Test
    void listarPedidos() throws Exception {
        Pedido otro = new Pedido(2L, "PED-0002", LocalDateTime.of(2026, 10, 6, 12, 0), LocalDateTime.of(2026, 10, 7, 12, 0), EstadoPedido.RETIRADO, CanalVenta.TIENDA, 79990, 0, 79990, "No aplica", "11111111-1", 3L, 2L, null);
        when(pedidoService.listarPedidos()).thenReturn(Arrays.asList(pedido, otro));

        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pedidoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.pedidoList[0].numero").value("PED-0001"))
                .andExpect(jsonPath("$._embedded.pedidoList[1].numero").value("PED-0002"))
                .andExpect(jsonPath("$._embedded.pedidoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(pedidoService.findById(1L)).thenReturn(Optional.of(pedido));

        mockMvc.perform(get("/api/v1/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPedido").value(1))
                .andExpect(jsonPath("$.numero").value("PED-0001"))
                .andExpect(jsonPath("$.fechaCreacion").exists())
                .andExpect(jsonPath("$.fechaExpiracion").exists())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_PAGO"))
                .andExpect(jsonPath("$.canal").value("WEB"))
                .andExpect(jsonPath("$.subtotal").value(899990))
                .andExpect(jsonPath("$.descuento").value(0))
                .andExpect(jsonPath("$.total").value(899990))
                .andExpect(jsonPath("$.motivoAnulacion").value("No aplica"))
                .andExpect(jsonPath("$.rutCliente").value("12345678-5"))
                .andExpect(jsonPath("$.idCupon").value(2))
                .andExpect(jsonPath("$.idEmpleado").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pedidos.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(pedidoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/pedidos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorNumeroExistente() throws Exception {
        when(pedidoService.findByNumero("PED-0001")).thenReturn(pedido);

        mockMvc.perform(get("/api/v1/pedidos/numero/PED-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPedido").value(1))
                .andExpect(jsonPath("$.numero").value("PED-0001"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pedidos.href").exists());
    }

    @Test
    void obtenerPorNumeroNoExistente() throws Exception {
        when(pedidoService.findByNumero("NO-EXISTE")).thenReturn(null);

        mockMvc.perform(get("/api/v1/pedidos/numero/NO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearPedido() throws Exception {
        when(pedidoService.guardarPedido(any(Pedido.class))).thenReturn(pedido);

        mockMvc.perform(post("/api/v1/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPedido").value(1))
                .andExpect(jsonPath("$.total").value(899990))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pedidos.href").exists());

        verify(pedidoService, times(1)).guardarPedido(any(Pedido.class));
    }

    @Test
    void crearPedidoConRelacionNoExistente() throws Exception {
        when(pedidoService.guardarPedido(any(Pedido.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearPedidoDuplicado() throws Exception {
        when(pedidoService.guardarPedido(any(Pedido.class)))
                .thenThrow(new DataIntegrityViolationException("valor duplicado"));

        mockMvc.perform(post("/api/v1/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarPedidoExistente() throws Exception {
        Pedido actualizado = new Pedido(1L, "PED-0003", LocalDateTime.of(2026, 10, 7, 9, 0), LocalDateTime.of(2026, 10, 8, 9, 0), EstadoPedido.PAGADO, CanalVenta.WEB, 899990, 50000, 849990, "No aplica", "12345678-5", 2L, 1L, null);
        when(pedidoService.actualizarPedido(eq(1L), any(Pedido.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/pedidos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(849990))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pedidos.href").exists());
    }

    @Test
    void actualizarPedidoNoExistente() throws Exception {
        when(pedidoService.actualizarPedido(eq(99L), any(Pedido.class)))
                .thenThrow(new RuntimeException("Pedido no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/pedidos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarPedido() throws Exception {
        doNothing().when(pedidoService).eliminarPedido(1L);

        mockMvc.perform(delete("/api/v1/pedidos/1"))
                .andExpect(status().isNoContent());

        verify(pedidoService, times(1)).eliminarPedido(1L);
    }

    @Test
    void eliminarPedidoEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(pedidoService).eliminarPedido(1L);

        mockMvc.perform(delete("/api/v1/pedidos/1"))
                .andExpect(status().isConflict());
    }
}
