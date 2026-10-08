package com.ridertapia.inventario.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.inventario.LimpiezaBaseDatos;
import com.ridertapia.inventario.model.ItemInventario;

import com.ridertapia.inventario.repository.ItemInventarioRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemInventarioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    // Simula los otros microservicios: los tests no dependen de que estén levantados
    @MockitoBean
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));
    }

    private ItemInventario nuevoItemInventario(int variante) {
        return switch (variante) {
            case 1 -> new ItemInventario(null, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)));
            case 2 -> new ItemInventario(null, 11L, 0, 0, 3, 45000, LocalDateTime.of(2026, 10, 6, 12, 0), new ArrayList<>(List.of(3L)));
            default -> new ItemInventario(null, 12L, 12, 5, 4, 640000, LocalDateTime.of(2026, 10, 7, 9, 0), new ArrayList<>(List.of(1L)));
        };
    }

    @Test
    void crearYListarItemsInventario() throws Exception {
        mockMvc.perform(post("/api/v1/items-inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoItemInventario(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idItemInventario").exists())
                .andExpect(jsonPath("$.stockDisponible").value(15));

        mockMvc.perform(post("/api/v1/items-inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoItemInventario(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/items-inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.itemInventarioList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/items-inventario/idVariante/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVariante").value(11L));
    }

    @Test
    void actualizarItemInventario() throws Exception {
        ItemInventario guardado = itemInventarioRepository.save(nuevoItemInventario(1));

        mockMvc.perform(put("/api/v1/items-inventario/" + guardado.getIdItemInventario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoItemInventario(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItemInventario").value(guardado.getIdItemInventario().intValue()))
                .andExpect(jsonPath("$.stockDisponible").value(12))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.itemsInventario.href").exists());
    }

    @Test
    void crearItemInventarioConVarianteNoExistente() throws Exception {
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/items-inventario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoItemInventario(1))))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/items-inventario"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        ItemInventario guardado = itemInventarioRepository.save(nuevoItemInventario(1));

        mockMvc.perform(delete("/api/v1/items-inventario/" + guardado.getIdItemInventario()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/items-inventario/" + guardado.getIdItemInventario()))
                .andExpect(status().isNotFound());
    }
}
