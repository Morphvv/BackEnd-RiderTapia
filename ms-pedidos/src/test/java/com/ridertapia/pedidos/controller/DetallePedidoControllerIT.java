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
import com.ridertapia.pedidos.model.DetallePedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.DetallePedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DetallePedidoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Pedido pedido;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        pedido = pedidoRepository.save(new Pedido(null, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    private DetallePedido nuevoDetallePedido(int variante) {
        return switch (variante) {
            case 1 -> new DetallePedido(null, 10L, 1, 899990, 899990, pedido);
            case 2 -> new DetallePedido(null, 11L, 2, 39995, 79990, pedido);
            default -> new DetallePedido(null, 10L, 3, 899990, 2699970, pedido);
        };
    }

    @Test
    void crearYListarDetallesPedido() throws Exception {
        mockMvc.perform(post("/api/v1/detalles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDetallePedido(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDetallePedido").exists())
                .andExpect(jsonPath("$.cantidad").value(1));

        mockMvc.perform(post("/api/v1/detalles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDetallePedido(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/detalles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.detallePedidoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/detalles/pedido/" + pedido.getIdPedido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.detallePedidoList", hasSize(2)));
    }

    @Test
    void actualizarDetallePedido() throws Exception {
        DetallePedido guardado = detallePedidoRepository.save(nuevoDetallePedido(1));

        mockMvc.perform(put("/api/v1/detalles/" + guardado.getIdDetallePedido())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDetallePedido(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDetallePedido").value(guardado.getIdDetallePedido().intValue()))
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.detalles.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        DetallePedido guardado = detallePedidoRepository.save(nuevoDetallePedido(1));

        mockMvc.perform(delete("/api/v1/detalles/" + guardado.getIdDetallePedido()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/detalles/" + guardado.getIdDetallePedido()))
                .andExpect(status().isNotFound());
    }
}
