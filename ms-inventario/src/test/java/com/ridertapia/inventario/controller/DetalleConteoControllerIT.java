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
import com.ridertapia.inventario.model.DetalleConteo;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.model.EstadoToma;
import com.ridertapia.inventario.repository.DetalleConteoRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;
import com.ridertapia.inventario.repository.TomaInventarioRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DetalleConteoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private DetalleConteoRepository detalleConteoRepository;

    @Autowired
    private TomaInventarioRepository tomaInventarioRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private TomaInventario tomaInventario;
    private ItemInventario itemInventario;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        tomaInventario = tomaInventarioRepository.save(new TomaInventario(null, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L));
        itemInventario = itemInventarioRepository.save(new ItemInventario(null, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    private DetalleConteo nuevoDetalleConteo(int variante) {
        return switch (variante) {
            case 1 -> new DetalleConteo(null, 15, 14, -1, tomaInventario, itemInventario);
            case 2 -> new DetalleConteo(null, 8, 8, 0, tomaInventario, itemInventario);
            default -> new DetalleConteo(null, 15, 12, -3, tomaInventario, itemInventario);
        };
    }

    @Test
    void crearYListarDetallesConteo() throws Exception {
        mockMvc.perform(post("/api/v1/detalles-conteo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDetalleConteo(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDetalleConteo").exists())
                .andExpect(jsonPath("$.cantidadContada").value(14));

        mockMvc.perform(post("/api/v1/detalles-conteo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDetalleConteo(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/detalles-conteo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.detalleConteoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/detalles-conteo/tomaInventario/" + tomaInventario.getIdTomaInventario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.detalleConteoList", hasSize(2)));
    }

    @Test
    void actualizarDetalleConteo() throws Exception {
        DetalleConteo guardado = detalleConteoRepository.save(nuevoDetalleConteo(1));

        mockMvc.perform(put("/api/v1/detalles-conteo/" + guardado.getIdDetalleConteo())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoDetalleConteo(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDetalleConteo").value(guardado.getIdDetalleConteo().intValue()))
                .andExpect(jsonPath("$.cantidadContada").value(12))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.detallesConteo.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        DetalleConteo guardado = detalleConteoRepository.save(nuevoDetalleConteo(1));

        mockMvc.perform(delete("/api/v1/detalles-conteo/" + guardado.getIdDetalleConteo()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/detalles-conteo/" + guardado.getIdDetalleConteo()))
                .andExpect(status().isNotFound());
    }
}
