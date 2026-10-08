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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.carrito.LimpiezaBaseDatos;
import com.ridertapia.carrito.model.Carrito;
import com.ridertapia.carrito.model.EstadoCarrito;
import com.ridertapia.carrito.repository.CarritoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CarritoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private CarritoRepository carritoRepository;

    // Simula ms-cliente: el RUT siempre existe salvo que el test diga lo contrario
    @MockitoBean
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));
    }

    private Carrito nuevoCarrito(int variante) {
        return switch (variante) {
            case 1 -> new Carrito(null, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L);
            case 2 -> new Carrito(null, "11111111-1", "tok-def456", LocalDateTime.of(2026, 10, 6, 12, 0), LocalDateTime.of(2026, 10, 6, 12, 30), EstadoCarrito.ACTIVO, 79990, 0, 79990, 3L);
            default -> new Carrito(null, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 7, 9, 0), LocalDateTime.of(2026, 10, 7, 9, 30), EstadoCarrito.CONVERTIDO_EN_PEDIDO, 899990, 89999, 809991, 2L);
        };
    }

    @Test
    void crearYListarCarritos() throws Exception {
        mockMvc.perform(post("/api/v1/carritos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCarrito(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCarrito").exists())
                .andExpect(jsonPath("$.total").value(899990));

        mockMvc.perform(post("/api/v1/carritos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCarrito(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/carritos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.carritoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/carritos/rutCliente/12345678-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.carritoList", hasSize(1)));
    }

    @Test
    void crearCarritoConClienteNoExistenteResponde404() throws Exception {
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/carritos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCarrito(1))))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarCarrito() throws Exception {
        Carrito guardado = carritoRepository.save(nuevoCarrito(1));

        mockMvc.perform(put("/api/v1/carritos/" + guardado.getIdCarrito())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCarrito(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito").value(guardado.getIdCarrito().intValue()))
                .andExpect(jsonPath("$.total").value(809991))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.carritos.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Carrito guardado = carritoRepository.save(nuevoCarrito(1));

        mockMvc.perform(delete("/api/v1/carritos/" + guardado.getIdCarrito()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/carritos/" + guardado.getIdCarrito()))
                .andExpect(status().isNotFound());
    }
}
