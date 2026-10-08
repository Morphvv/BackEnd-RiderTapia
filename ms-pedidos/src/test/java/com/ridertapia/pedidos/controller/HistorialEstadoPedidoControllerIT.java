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
import com.ridertapia.pedidos.model.HistorialEstadoPedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.HistorialEstadoPedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HistorialEstadoPedidoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private HistorialEstadoPedidoRepository historialEstadoPedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Pedido pedido;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        pedido = pedidoRepository.save(new Pedido(null, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    private HistorialEstadoPedido nuevoHistorialEstadoPedido(int variante) {
        return switch (variante) {
            case 1 -> new HistorialEstadoPedido(null, EstadoPedido.PENDIENTE_PAGO, EstadoPedido.PAGADO, LocalDateTime.of(2026, 10, 6, 10, 0), 1L, pedido);
            case 2 -> new HistorialEstadoPedido(null, EstadoPedido.PAGADO, EstadoPedido.EN_PREPARACION, LocalDateTime.of(2026, 10, 6, 12, 0), 2L, pedido);
            default -> new HistorialEstadoPedido(null, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO_PARA_RETIRO, LocalDateTime.of(2026, 10, 7, 9, 0), 3L, pedido);
        };
    }

    @Test
    void crearYListarHistorialesEstadoPedido() throws Exception {
        mockMvc.perform(post("/api/v1/historial-estados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoHistorialEstadoPedido(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idHistorialEstadoPedido").exists())
                .andExpect(jsonPath("$.estadoNuevo").value("PAGADO"));

        mockMvc.perform(post("/api/v1/historial-estados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoHistorialEstadoPedido(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/historial-estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.historialEstadoPedidoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/historial-estados/pedido/" + pedido.getIdPedido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.historialEstadoPedidoList", hasSize(2)));
    }

    @Test
    void actualizarHistorialEstadoPedido() throws Exception {
        HistorialEstadoPedido guardado = historialEstadoPedidoRepository.save(nuevoHistorialEstadoPedido(1));

        mockMvc.perform(put("/api/v1/historial-estados/" + guardado.getIdHistorialEstadoPedido())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoHistorialEstadoPedido(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idHistorialEstadoPedido").value(guardado.getIdHistorialEstadoPedido().intValue()))
                .andExpect(jsonPath("$.estadoNuevo").value("LISTO_PARA_RETIRO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.historialEstados.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        HistorialEstadoPedido guardado = historialEstadoPedidoRepository.save(nuevoHistorialEstadoPedido(1));

        mockMvc.perform(delete("/api/v1/historial-estados/" + guardado.getIdHistorialEstadoPedido()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/historial-estados/" + guardado.getIdHistorialEstadoPedido()))
                .andExpect(status().isNotFound());
    }
}
