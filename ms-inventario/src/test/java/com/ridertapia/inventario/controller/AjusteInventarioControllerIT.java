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
import com.ridertapia.inventario.model.AjusteInventario;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.EstadoToma;
import com.ridertapia.inventario.model.MotivoAjuste;
import com.ridertapia.inventario.model.TipoMovimiento;
import com.ridertapia.inventario.repository.AjusteInventarioRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AjusteInventarioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private AjusteInventarioRepository ajusteInventarioRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ItemInventario itemInventario;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        itemInventario = itemInventarioRepository.save(new ItemInventario(null, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    private AjusteInventario nuevoAjusteInventario(int variante) {
        return switch (variante) {
            case 1 -> new AjusteInventario(null, TipoMovimiento.INGRESO, 10, 5, 15, LocalDateTime.of(2026, 10, 6, 9, 0), "Recepción de factura", 1L, 5L, 3L, itemInventario, MotivoAjuste.MERMA, null);
            case 2 -> new AjusteInventario(null, TipoMovimiento.VENTA_WEB, 2, 15, 13, LocalDateTime.of(2026, 10, 6, 12, 0), "Venta pedido PED-0001", 2L, 6L, 4L, itemInventario, MotivoAjuste.ERROR_CONTEO, null);
            default -> new AjusteInventario(null, TipoMovimiento.AJUSTE, 3, 13, 10, LocalDateTime.of(2026, 10, 7, 9, 0), "Ajuste por conteo", 1L, 5L, 3L, itemInventario, MotivoAjuste.CONTEO_FISICO, null);
        };
    }

    @Test
    void crearYListarAjustesInventario() throws Exception {
        mockMvc.perform(post("/api/v1/ajustes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoAjusteInventario(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMovimientoInventario").exists())
                .andExpect(jsonPath("$.motivo").value("MERMA"));

        mockMvc.perform(post("/api/v1/ajustes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoAjusteInventario(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/ajustes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ajusteInventarioList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/ajustes/itemInventario/" + itemInventario.getIdItemInventario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ajusteInventarioList", hasSize(2)));
    }

    @Test
    void actualizarAjusteInventario() throws Exception {
        AjusteInventario guardado = ajusteInventarioRepository.save(nuevoAjusteInventario(1));

        mockMvc.perform(put("/api/v1/ajustes/" + guardado.getIdMovimientoInventario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoAjusteInventario(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMovimientoInventario").value(guardado.getIdMovimientoInventario().intValue()))
                .andExpect(jsonPath("$.motivo").value("CONTEO_FISICO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ajustes.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        AjusteInventario guardado = ajusteInventarioRepository.save(nuevoAjusteInventario(1));

        mockMvc.perform(delete("/api/v1/ajustes/" + guardado.getIdMovimientoInventario()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/ajustes/" + guardado.getIdMovimientoInventario()))
                .andExpect(status().isNotFound());
    }
}
