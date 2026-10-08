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
import com.ridertapia.inventario.model.IngresoMercancia;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TipoMovimiento;
import com.ridertapia.inventario.repository.IngresoMercanciaRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IngresoMercanciaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private IngresoMercanciaRepository ingresoMercanciaRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ItemInventario itemInventario;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        itemInventario = itemInventarioRepository.save(new ItemInventario(null, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    private IngresoMercancia nuevoIngresoMercancia(int variante) {
        return switch (variante) {
            case 1 -> new IngresoMercancia(null, TipoMovimiento.INGRESO, 10, 5, 15, LocalDateTime.of(2026, 10, 6, 9, 0), "Recepción de factura", 1L, 5L, 3L, itemInventario, 640000, "FAC-5501", 1L);
            case 2 -> new IngresoMercancia(null, TipoMovimiento.VENTA_WEB, 2, 15, 13, LocalDateTime.of(2026, 10, 6, 12, 0), "Venta pedido PED-0001", 2L, 6L, 4L, itemInventario, 650000, "FAC-5502", 2L);
            default -> new IngresoMercancia(null, TipoMovimiento.AJUSTE, 3, 13, 10, LocalDateTime.of(2026, 10, 7, 9, 0), "Ajuste por conteo", 1L, 5L, 3L, itemInventario, 630000, "FAC-5503", 1L);
        };
    }

    @Test
    void crearYListarIngresosMercancia() throws Exception {
        mockMvc.perform(post("/api/v1/ingresos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoIngresoMercancia(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimientoInventario").exists())
                .andExpect(jsonPath("$.numeroDocumento").value("FAC-5501"));

        mockMvc.perform(post("/api/v1/ingresos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoIngresoMercancia(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/ingresos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ingresoMercanciaList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/ingresos/itemInventario/" + itemInventario.getIdItemInventario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ingresoMercanciaList", hasSize(2)));
    }

    @Test
    void actualizarIngresoMercancia() throws Exception {
        IngresoMercancia guardado = ingresoMercanciaRepository.save(nuevoIngresoMercancia(1));

        mockMvc.perform(put("/api/v1/ingresos/" + guardado.getIdMovimientoInventario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoIngresoMercancia(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMovimientoInventario").value(guardado.getIdMovimientoInventario().intValue()))
                .andExpect(jsonPath("$.numeroDocumento").value("FAC-5503"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ingresos.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        IngresoMercancia guardado = ingresoMercanciaRepository.save(nuevoIngresoMercancia(1));

        mockMvc.perform(delete("/api/v1/ingresos/" + guardado.getIdMovimientoInventario()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/ingresos/" + guardado.getIdMovimientoInventario()))
                .andExpect(status().isNotFound());
    }
}
