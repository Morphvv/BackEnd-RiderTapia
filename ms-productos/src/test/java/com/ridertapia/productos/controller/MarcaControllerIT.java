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
import com.ridertapia.productos.model.Marca;

import com.ridertapia.productos.repository.MarcaRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarcaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private MarcaRepository marcaRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();


    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();

    }

    private Marca nuevoMarca(int variante) {
        return switch (variante) {
            case 1 -> new Marca(null, "Trek", true);
            case 2 -> new Marca(null, "Specialized", true);
            default -> new Marca(null, "Trek Bikes", false);
        };
    }

    @Test
    void crearYListarMarcas() throws Exception {
        mockMvc.perform(post("/api/v1/marcas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMarca(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idMarca").exists())
                .andExpect(jsonPath("$.nombre").value("Trek"));

        mockMvc.perform(post("/api/v1/marcas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMarca(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/marcas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.marcaList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/marcas/nombre/Specialized"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Specialized"));
    }

    @Test
    void actualizarMarca() throws Exception {
        Marca guardado = marcaRepository.save(nuevoMarca(1));

        mockMvc.perform(put("/api/v1/marcas/" + guardado.getIdMarca())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMarca(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMarca").value(guardado.getIdMarca().intValue()))
                .andExpect(jsonPath("$.nombre").value("Trek Bikes"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.marcas.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Marca guardado = marcaRepository.save(nuevoMarca(1));

        mockMvc.perform(delete("/api/v1/marcas/" + guardado.getIdMarca()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/marcas/" + guardado.getIdMarca()))
                .andExpect(status().isNotFound());
    }
}
