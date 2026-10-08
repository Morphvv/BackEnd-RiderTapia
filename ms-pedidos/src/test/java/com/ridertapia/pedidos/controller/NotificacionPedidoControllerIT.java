package com.ridertapia.pedidos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ridertapia.pedidos.LimpiezaBaseDatos;
import com.ridertapia.pedidos.model.NotificacionPedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.NotificacionPedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificacionPedidoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private NotificacionPedidoRepository notificacionPedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Pedido pedido;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        pedido = pedidoRepository.save(new Pedido(null, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    private NotificacionPedido nuevoNotificacionPedido(int variante) {
        return switch (variante) {
            case 1 -> new NotificacionPedido(null, "CONFIRMACION_COMPRA", "ana@correo.cl", LocalDateTime.of(2026, 10, 6, 10, 0), true, pedido);
            case 2 -> new NotificacionPedido(null, "LISTO_PARA_RETIRO", "ana@correo.cl", LocalDateTime.of(2026, 10, 6, 12, 0), false, pedido);
            default -> new NotificacionPedido(null, "PEDIDO_RETIRADO", "ana@correo.cl", LocalDateTime.of(2026, 10, 7, 9, 0), true, pedido);
        };
    }

    @Test
    void crearYListarNotificacionesPedido() throws Exception {
        mockMvc.perform(post("/api/v1/notificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoNotificacionPedido(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idNotificacionPedido").exists())
                .andExpect(jsonPath("$.tipo").value("CONFIRMACION_COMPRA"));

        mockMvc.perform(post("/api/v1/notificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoNotificacionPedido(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notificacionPedidoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/notificaciones/pedido/" + pedido.getIdPedido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notificacionPedidoList", hasSize(2)));
    }

    @Test
    void actualizarNotificacionPedido() throws Exception {
        NotificacionPedido guardado = notificacionPedidoRepository.save(nuevoNotificacionPedido(1));

        mockMvc.perform(put("/api/v1/notificaciones/" + guardado.getIdNotificacionPedido())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoNotificacionPedido(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idNotificacionPedido").value(guardado.getIdNotificacionPedido().intValue()))
                .andExpect(jsonPath("$.tipo").value("PEDIDO_RETIRADO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.notificaciones.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        NotificacionPedido guardado = notificacionPedidoRepository.save(nuevoNotificacionPedido(1));

        mockMvc.perform(delete("/api/v1/notificaciones/" + guardado.getIdNotificacionPedido()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/notificaciones/" + guardado.getIdNotificacionPedido()))
                .andExpect(status().isNotFound());
    }
}
