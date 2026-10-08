package com.ridertapia.inventario.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ridertapia.inventario.LimpiezaBaseDatos;
import com.ridertapia.inventario.model.AlertaStock;
import com.ridertapia.inventario.model.ItemInventario;

import com.ridertapia.inventario.repository.AlertaStockRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertaStockControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private AlertaStockRepository alertaStockRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ItemInventario itemInventario;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        itemInventario = itemInventarioRepository.save(new ItemInventario(null, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    private AlertaStock nuevoAlertaStock(int variante) {
        return switch (variante) {
            case 1 -> new AlertaStock(null, LocalDateTime.of(2026, 10, 6, 9, 0), 2, false, itemInventario);
            case 2 -> new AlertaStock(null, LocalDateTime.of(2026, 10, 6, 12, 0), 1, false, itemInventario);
            default -> new AlertaStock(null, LocalDateTime.of(2026, 10, 7, 9, 0), 0, true, itemInventario);
        };
    }

    @Test
    void crearYListarAlertasStock() throws Exception {
        mockMvc.perform(post("/api/v1/alertas-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoAlertaStock(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAlertaStock").exists())
                .andExpect(jsonPath("$.stockAlMomento").value(2));

        mockMvc.perform(post("/api/v1/alertas-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoAlertaStock(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/alertas-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.alertaStockList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/alertas-stock/itemInventario/" + itemInventario.getIdItemInventario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.alertaStockList", hasSize(2)));
    }

    @Test
    void actualizarAlertaStock() throws Exception {
        AlertaStock guardado = alertaStockRepository.save(nuevoAlertaStock(1));

        mockMvc.perform(put("/api/v1/alertas-stock/" + guardado.getIdAlertaStock())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoAlertaStock(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlertaStock").value(guardado.getIdAlertaStock().intValue()))
                .andExpect(jsonPath("$.stockAlMomento").value(0))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.alertasStock.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        AlertaStock guardado = alertaStockRepository.save(nuevoAlertaStock(1));

        mockMvc.perform(delete("/api/v1/alertas-stock/" + guardado.getIdAlertaStock()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/alertas-stock/" + guardado.getIdAlertaStock()))
                .andExpect(status().isNotFound());
    }
}
