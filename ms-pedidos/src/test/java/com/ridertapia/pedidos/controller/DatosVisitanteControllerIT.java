package com.ridertapia.pedidos.controller;

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

import com.ridertapia.pedidos.LimpiezaBaseDatos;
import com.ridertapia.pedidos.model.DatosVisitante;

import com.ridertapia.pedidos.repository.DatosVisitanteRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DatosVisitanteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private DatosVisitanteRepository datosVisitanteRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();


    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();

    }

    private DatosVisitante nuevoDatosVisitante(int variante) {
        return switch (variante) {
            case 1 -> new DatosVisitante(null, "Ana Soto", "ana@correo.cl", "+56911112222");
            case 2 -> new DatosVisitante(null, "Luis Rojas", "luis@correo.cl", "+56933334444");
            default -> new DatosVisitante(null, "Ana Soto Pérez", "ana.soto@correo.cl", "+56955556666");
        };
    }

    @Test
    void crearYListarDatosVisitantes() throws Exception {
        mockMvc.perform(post("/api/v1/visitantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDatosVisitante(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDatosVisitante").exists())
                .andExpect(jsonPath("$.nombre").value("Ana Soto"));

        mockMvc.perform(post("/api/v1/visitantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDatosVisitante(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/visitantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.datosVisitanteList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/visitantes/correo/ana@correo.cl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.datosVisitanteList", hasSize(1)));
    }

    @Test
    void actualizarDatosVisitante() throws Exception {
        DatosVisitante guardado = datosVisitanteRepository.save(nuevoDatosVisitante(1));

        mockMvc.perform(put("/api/v1/visitantes/" + guardado.getIdDatosVisitante())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDatosVisitante(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDatosVisitante").value(guardado.getIdDatosVisitante().intValue()))
                .andExpect(jsonPath("$.nombre").value("Ana Soto Pérez"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.visitantes.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        DatosVisitante guardado = datosVisitanteRepository.save(nuevoDatosVisitante(1));

        mockMvc.perform(delete("/api/v1/visitantes/" + guardado.getIdDatosVisitante()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/visitantes/" + guardado.getIdDatosVisitante()))
                .andExpect(status().isNotFound());
    }
}
