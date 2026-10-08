package com.ridertapia.pedidos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import com.ridertapia.pedidos.model.Boleta;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.BoletaRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BoletaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private BoletaRepository boletaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Pedido pedido;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        pedido = pedidoRepository.save(new Pedido(null, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    private Boleta nuevoBoleta(int variante) {
        return switch (variante) {
            case 1 -> new Boleta(null, 1001, LocalDate.of(2026, 10, 6), 756294, 143696, 899990, "https://docs.ridertapia.cl/1001.pdf", pedido);
            case 2 -> new Boleta(null, 1002, LocalDate.of(2026, 10, 7), 67218, 12772, 79990, "https://docs.ridertapia.cl/1002.pdf", pedido);
            default -> new Boleta(null, 1003, LocalDate.of(2026, 10, 8), 922681, 175309, 1097990, "https://docs.ridertapia.cl/1003.pdf", pedido);
        };
    }

    @Test
    void crearYListarBoletas() throws Exception {
        mockMvc.perform(post("/api/v1/boletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoBoleta(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDocumentoTributario").exists())
                .andExpect(jsonPath("$.folio").value(1001));

        mockMvc.perform(post("/api/v1/boletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoBoleta(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/boletas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.boletaList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/boletas/pedido/" + pedido.getIdPedido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.boletaList", hasSize(2)));
    }

    @Test
    void actualizarBoleta() throws Exception {
        Boleta guardado = boletaRepository.save(nuevoBoleta(1));

        mockMvc.perform(put("/api/v1/boletas/" + guardado.getIdDocumentoTributario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoBoleta(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDocumentoTributario").value(guardado.getIdDocumentoTributario().intValue()))
                .andExpect(jsonPath("$.folio").value(1003))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.boletas.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Boleta guardado = boletaRepository.save(nuevoBoleta(1));

        mockMvc.perform(delete("/api/v1/boletas/" + guardado.getIdDocumentoTributario()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/boletas/" + guardado.getIdDocumentoTributario()))
                .andExpect(status().isNotFound());
    }
}
