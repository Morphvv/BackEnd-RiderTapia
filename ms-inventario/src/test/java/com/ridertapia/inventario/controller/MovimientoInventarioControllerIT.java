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
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.MovimientoInventario;
import com.ridertapia.inventario.model.TipoMovimiento;
import com.ridertapia.inventario.repository.ItemInventarioRepository;
import com.ridertapia.inventario.repository.MovimientoInventarioRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MovimientoInventarioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ItemInventario itemInventario;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        itemInventario = itemInventarioRepository.save(new ItemInventario(null, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    private MovimientoInventario nuevoMovimientoInventario(int variante) {
        return switch (variante) {
            case 1 -> new MovimientoInventario(null, TipoMovimiento.INGRESO, 10, 5, 15, LocalDateTime.of(2026, 10, 6, 9, 0), "Recepción de factura", 1L, 5L, 3L, itemInventario);
            case 2 -> new MovimientoInventario(null, TipoMovimiento.VENTA_WEB, 2, 15, 13, LocalDateTime.of(2026, 10, 6, 12, 0), "Venta pedido PED-0001", 2L, 6L, 4L, itemInventario);
            default -> new MovimientoInventario(null, TipoMovimiento.AJUSTE, 3, 13, 10, LocalDateTime.of(2026, 10, 7, 9, 0), "Ajuste por conteo", 1L, 5L, 3L, itemInventario);
        };
    }

    @Test
    void crearYListarMovimientosInventario() throws Exception {
        mockMvc.perform(post("/api/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMovimientoInventario(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimientoInventario").exists())
                .andExpect(jsonPath("$.cantidad").value(10));

        mockMvc.perform(post("/api/v1/movimientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMovimientoInventario(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.movimientoInventarioList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/movimientos/itemInventario/" + itemInventario.getIdItemInventario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.movimientoInventarioList", hasSize(2)));
    }

    @Test
    void actualizarMovimientoInventario() throws Exception {
        MovimientoInventario guardado = movimientoInventarioRepository.save(nuevoMovimientoInventario(1));

        mockMvc.perform(put("/api/v1/movimientos/" + guardado.getIdMovimientoInventario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMovimientoInventario(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMovimientoInventario").value(guardado.getIdMovimientoInventario().intValue()))
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.movimientos.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        MovimientoInventario guardado = movimientoInventarioRepository.save(nuevoMovimientoInventario(1));

        mockMvc.perform(delete("/api/v1/movimientos/" + guardado.getIdMovimientoInventario()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/movimientos/" + guardado.getIdMovimientoInventario()))
                .andExpect(status().isNotFound());
    }
}
