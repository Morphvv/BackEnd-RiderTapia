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
import com.ridertapia.productos.model.Categoria;

import com.ridertapia.productos.repository.CategoriaRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoriaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();


    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();

    }

    private Categoria nuevoCategoria(int variante) {
        return switch (variante) {
            case 1 -> new Categoria(null, "Bicicletas", "Bicicletas de todo tipo", true, null);
            case 2 -> new Categoria(null, "Accesorios", "Cascos, luces y repuestos", true, null);
            default -> new Categoria(null, "Bicicletas MTB", "Bicicletas de montaña", false, null);
        };
    }

    @Test
    void crearYListarCategorias() throws Exception {
        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCategoria(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCategoria").exists())
                .andExpect(jsonPath("$.nombre").value("Bicicletas"));

        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCategoria(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.categoriaList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/categorias/nombre/Accesorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Accesorios"));
    }

    @Test
    void actualizarCategoria() throws Exception {
        Categoria guardado = categoriaRepository.save(nuevoCategoria(1));

        mockMvc.perform(put("/api/v1/categorias/" + guardado.getIdCategoria())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCategoria(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(guardado.getIdCategoria().intValue()))
                .andExpect(jsonPath("$.nombre").value("Bicicletas MTB"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.categorias.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Categoria guardado = categoriaRepository.save(nuevoCategoria(1));

        mockMvc.perform(delete("/api/v1/categorias/" + guardado.getIdCategoria()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/categorias/" + guardado.getIdCategoria()))
                .andExpect(status().isNotFound());
    }
}
