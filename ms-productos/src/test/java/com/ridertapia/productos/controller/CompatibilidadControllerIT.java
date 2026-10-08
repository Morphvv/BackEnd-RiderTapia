package com.ridertapia.productos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ridertapia.productos.LimpiezaBaseDatos;
import com.ridertapia.productos.model.Compatibilidad;

import com.ridertapia.productos.repository.CompatibilidadRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompatibilidadControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private CompatibilidadRepository compatibilidadRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();


    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();

    }

    private Compatibilidad nuevoCompatibilidad(int variante) {
        return switch (variante) {
            case 1 -> new Compatibilidad(null, "MTB", "29", "12");
            case 2 -> new Compatibilidad(null, "Ruta", "700c", "11");
            default -> new Compatibilidad(null, "MTB", "27.5", "10");
        };
    }

    @Test
    void crearYListarCompatibilidades() throws Exception {
        mockMvc.perform(post("/api/v1/compatibilidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCompatibilidad(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCompatibilidad").exists())
                .andExpect(jsonPath("$.aro").value("29"));

        mockMvc.perform(post("/api/v1/compatibilidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCompatibilidad(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/compatibilidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.compatibilidadList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/compatibilidades/tipoBicicleta/MTB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.compatibilidadList", hasSize(1)));
    }

    @Test
    void actualizarCompatibilidad() throws Exception {
        Compatibilidad guardado = compatibilidadRepository.save(nuevoCompatibilidad(1));

        mockMvc.perform(put("/api/v1/compatibilidades/" + guardado.getIdCompatibilidad())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCompatibilidad(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCompatibilidad").value(guardado.getIdCompatibilidad().intValue()))
                .andExpect(jsonPath("$.aro").value("27.5"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.compatibilidades.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Compatibilidad guardado = compatibilidadRepository.save(nuevoCompatibilidad(1));

        mockMvc.perform(delete("/api/v1/compatibilidades/" + guardado.getIdCompatibilidad()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/compatibilidades/" + guardado.getIdCompatibilidad()))
                .andExpect(status().isNotFound());
    }
}
