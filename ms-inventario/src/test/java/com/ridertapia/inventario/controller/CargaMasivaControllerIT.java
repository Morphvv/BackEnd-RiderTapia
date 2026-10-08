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
import com.ridertapia.inventario.model.CargaMasiva;

import com.ridertapia.inventario.repository.CargaMasivaRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CargaMasivaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private CargaMasivaRepository cargaMasivaRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
    }

    private CargaMasiva nuevoCargaMasiva(int variante) {
        return switch (variante) {
            case 1 -> new CargaMasiva(null, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L);
            case 2 -> new CargaMasiva(null, "repuestos.xlsx", LocalDateTime.of(2026, 10, 6, 12, 0), 40, 0, 2L);
            default -> new CargaMasiva(null, "productos_octubre_v2.xlsx", LocalDateTime.of(2026, 10, 7, 9, 0), 118, 1, 1L);
        };
    }

    @Test
    void crearYListarCargasMasivas() throws Exception {
        mockMvc.perform(post("/api/v1/cargas-masivas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCargaMasiva(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCargaMasiva").exists())
                .andExpect(jsonPath("$.filasProcesadas").value(120));

        mockMvc.perform(post("/api/v1/cargas-masivas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCargaMasiva(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/cargas-masivas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cargaMasivaList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/cargas-masivas/idEmpleado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cargaMasivaList", hasSize(1)));
    }

    @Test
    void actualizarCargaMasiva() throws Exception {
        CargaMasiva guardado = cargaMasivaRepository.save(nuevoCargaMasiva(1));

        mockMvc.perform(put("/api/v1/cargas-masivas/" + guardado.getIdCargaMasiva())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCargaMasiva(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCargaMasiva").value(guardado.getIdCargaMasiva().intValue()))
                .andExpect(jsonPath("$.filasProcesadas").value(118))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cargasMasivas.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        CargaMasiva guardado = cargaMasivaRepository.save(nuevoCargaMasiva(1));

        mockMvc.perform(delete("/api/v1/cargas-masivas/" + guardado.getIdCargaMasiva()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cargas-masivas/" + guardado.getIdCargaMasiva()))
                .andExpect(status().isNotFound());
    }
}
