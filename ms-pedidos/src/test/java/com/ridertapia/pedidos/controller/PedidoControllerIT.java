package com.ridertapia.pedidos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.pedidos.LimpiezaBaseDatos;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.PedidoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PedidoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private PedidoRepository pedidoRepository;

    // Simula ms-cliente: el RUT siempre existe salvo que el test diga lo contrario
    @MockitoBean
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();


    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));

    }

    private Pedido nuevoPedido(int variante) {
        return switch (variante) {
            case 1 -> new Pedido(null, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null);
            case 2 -> new Pedido(null, "PED-0002", LocalDateTime.of(2026, 10, 6, 12, 0), LocalDateTime.of(2026, 10, 7, 12, 0), EstadoPedido.RETIRADO, CanalVenta.TIENDA, 79990, 0, 79990, "No aplica", "11111111-1", 3L, 2L, null);
            default -> new Pedido(null, "PED-0003", LocalDateTime.of(2026, 10, 7, 9, 0), LocalDateTime.of(2026, 10, 8, 9, 0), EstadoPedido.PAGADO, CanalVenta.WEB, 899990, 50000, 849990, "No aplica", "12345678-5", 2L, 1L, null);
        };
    }

    @Test
    void crearYListarPedidos() throws Exception {
        mockMvc.perform(post("/api/v1/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPedido(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPedido").exists())
                .andExpect(jsonPath("$.total").value(899990));

        mockMvc.perform(post("/api/v1/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPedido(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pedidoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/pedidos/numero/PED-0002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("PED-0002"));
    }

    @Test
    void crearPedidoConClienteNoExistenteResponde404() throws Exception {
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPedido(1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarPedido() throws Exception {
        Pedido guardado = pedidoRepository.save(nuevoPedido(1));

        mockMvc.perform(put("/api/v1/pedidos/" + guardado.getIdPedido())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPedido(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPedido").value(guardado.getIdPedido().intValue()))
                .andExpect(jsonPath("$.total").value(849990))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pedidos.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Pedido guardado = pedidoRepository.save(nuevoPedido(1));

        mockMvc.perform(delete("/api/v1/pedidos/" + guardado.getIdPedido()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pedidos/" + guardado.getIdPedido()))
                .andExpect(status().isNotFound());
    }
}
