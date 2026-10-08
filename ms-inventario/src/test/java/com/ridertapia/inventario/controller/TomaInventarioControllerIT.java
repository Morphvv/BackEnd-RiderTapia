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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ridertapia.inventario.LimpiezaBaseDatos;
import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.model.EstadoToma;
import com.ridertapia.inventario.repository.TomaInventarioRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TomaInventarioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private TomaInventarioRepository tomaInventarioRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
    }

    private TomaInventario nuevoTomaInventario(int variante) {
        return switch (variante) {
            case 1 -> new TomaInventario(null, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L);
            case 2 -> new TomaInventario(null, LocalDateTime.of(2026, 10, 6, 12, 0), LocalDateTime.of(2026, 10, 6, 19, 0), EstadoToma.EN_CURSO, 2L);
            default -> new TomaInventario(null, LocalDateTime.of(2026, 10, 7, 9, 0), LocalDateTime.of(2026, 10, 7, 18, 0), EstadoToma.CONFIRMADA, 1L);
        };
    }

    @Test
    void crearYListarTomasInventario() throws Exception {
        mockMvc.perform(post("/api/v1/tomas-inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoTomaInventario(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idTomaInventario").exists())
                .andExpect(jsonPath("$.estado").value("EN_CURSO"));

        mockMvc.perform(post("/api/v1/tomas-inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoTomaInventario(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/tomas-inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tomaInventarioList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/tomas-inventario/idEmpleado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tomaInventarioList", hasSize(1)));
    }

    @Test
    void actualizarTomaInventario() throws Exception {
        TomaInventario guardado = tomaInventarioRepository.save(nuevoTomaInventario(1));

        mockMvc.perform(put("/api/v1/tomas-inventario/" + guardado.getIdTomaInventario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoTomaInventario(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTomaInventario").value(guardado.getIdTomaInventario().intValue()))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.tomasInventario.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        TomaInventario guardado = tomaInventarioRepository.save(nuevoTomaInventario(1));

        mockMvc.perform(delete("/api/v1/tomas-inventario/" + guardado.getIdTomaInventario()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/tomas-inventario/" + guardado.getIdTomaInventario()))
                .andExpect(status().isNotFound());
    }
}
