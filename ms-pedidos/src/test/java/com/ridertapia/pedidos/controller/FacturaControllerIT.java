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
import com.ridertapia.pedidos.model.Factura;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.FacturaRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FacturaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Pedido pedido;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        pedido = pedidoRepository.save(new Pedido(null, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    private Factura nuevoFactura(int variante) {
        return switch (variante) {
            case 1 -> new Factura(null, 1001, LocalDate.of(2026, 10, 6), 756294, 143696, 899990, "https://docs.ridertapia.cl/1001.pdf", pedido, "76123456-7", "Ciclismo Sur SpA", "Venta de artículos deportivos", "Barros Arana 123, Concepción");
            case 2 -> new Factura(null, 1002, LocalDate.of(2026, 10, 7), 67218, 12772, 79990, "https://docs.ridertapia.cl/1002.pdf", pedido, "77987654-3", "Ruedas del Biobío Ltda", "Taller de bicicletas", "O'Higgins 456, Concepción");
            default -> new Factura(null, 1003, LocalDate.of(2026, 10, 8), 922681, 175309, 1097990, "https://docs.ridertapia.cl/1003.pdf", pedido, "76123456-7", "Ciclismo Sur Limitada", "Comercio", "Colo Colo 789, Concepción");
        };
    }

    @Test
    void crearYListarFacturas() throws Exception {
        mockMvc.perform(post("/api/v1/facturas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoFactura(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDocumentoTributario").exists())
                .andExpect(jsonPath("$.razonSocial").value("Ciclismo Sur SpA"));

        mockMvc.perform(post("/api/v1/facturas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoFactura(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/facturas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.facturaList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/facturas/pedido/" + pedido.getIdPedido()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.facturaList", hasSize(2)));
    }

    @Test
    void actualizarFactura() throws Exception {
        Factura guardado = facturaRepository.save(nuevoFactura(1));

        mockMvc.perform(put("/api/v1/facturas/" + guardado.getIdDocumentoTributario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoFactura(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDocumentoTributario").value(guardado.getIdDocumentoTributario().intValue()))
                .andExpect(jsonPath("$.razonSocial").value("Ciclismo Sur Limitada"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.facturas.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Factura guardado = facturaRepository.save(nuevoFactura(1));

        mockMvc.perform(delete("/api/v1/facturas/" + guardado.getIdDocumentoTributario()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/facturas/" + guardado.getIdDocumentoTributario()))
                .andExpect(status().isNotFound());
    }
}
