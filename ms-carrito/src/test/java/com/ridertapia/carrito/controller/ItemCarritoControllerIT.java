package com.ridertapia.carrito.controller;

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
import java.util.Map;

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

import com.ridertapia.carrito.LimpiezaBaseDatos;
import com.ridertapia.carrito.model.Carrito;
import com.ridertapia.carrito.model.ItemCarrito;
import com.ridertapia.carrito.model.EstadoCarrito;
import com.ridertapia.carrito.repository.CarritoRepository;
import com.ridertapia.carrito.repository.ItemCarritoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemCarritoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    // Simula los otros microservicios: los tests no dependen de que estén levantados
    @MockitoBean
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Carrito carrito;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));
        when(restTemplate.getForObject(any(URI.class), eq(Map.class))).thenReturn(Map.of("stockDisponible", 100));
        carrito = carritoRepository.save(new Carrito(null, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L));
    }

    private ItemCarrito nuevoItemCarrito(int variante) {
        return switch (variante) {
            case 1 -> new ItemCarrito(null, 10L, 1, 899990, LocalDateTime.of(2026, 10, 6, 11, 0), carrito);
            case 2 -> new ItemCarrito(null, 11L, 2, 39995, LocalDateTime.of(2026, 10, 6, 12, 0), carrito);
            default -> new ItemCarrito(null, 10L, 3, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), carrito);
        };
    }

    @Test
    void crearYListarItemsCarrito() throws Exception {
        mockMvc.perform(post("/api/v1/items-carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoItemCarrito(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idItemCarrito").exists())
                .andExpect(jsonPath("$.cantidad").value(1));

        mockMvc.perform(post("/api/v1/items-carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoItemCarrito(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/items-carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.itemCarritoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/items-carrito/carrito/" + carrito.getIdCarrito()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.itemCarritoList", hasSize(2)));
    }

    @Test
    void actualizarItemCarrito() throws Exception {
        ItemCarrito guardado = itemCarritoRepository.save(nuevoItemCarrito(1));

        mockMvc.perform(put("/api/v1/items-carrito/" + guardado.getIdItemCarrito())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoItemCarrito(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idItemCarrito").value(guardado.getIdItemCarrito().intValue()))
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.itemsCarrito.href").exists());
    }

    @Test
    void crearItemCarritoConVarianteNoExistente() throws Exception {
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/items-carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoItemCarrito(1))))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/items-carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void crearItemCarritoSinStock() throws Exception {
        when(restTemplate.getForObject(any(URI.class), eq(Map.class))).thenReturn(Map.of("stockDisponible", 1));

        mockMvc.perform(post("/api/v1/items-carrito")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoItemCarrito(3))))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/v1/items-carrito"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        ItemCarrito guardado = itemCarritoRepository.save(nuevoItemCarrito(1));

        mockMvc.perform(delete("/api/v1/items-carrito/" + guardado.getIdItemCarrito()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/items-carrito/" + guardado.getIdItemCarrito()))
                .andExpect(status().isNotFound());
    }
}
