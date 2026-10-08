package com.ridertapia.pagos.controller;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.pagos.LimpiezaBaseDatos;
import com.ridertapia.pagos.model.PagoPresencial;
import com.ridertapia.pagos.model.EstadoPago;
import com.ridertapia.pagos.model.MedioPago;
import com.ridertapia.pagos.repository.PagoPresencialRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PagoPresencialControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private PagoPresencialRepository pagoPresencialRepository;

    // Simula los otros microservicios: los tests no dependen de que estén levantados
    @MockitoBean
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        when(restTemplate.getForEntity(any(URI.class), eq(String.class))).thenReturn(ResponseEntity.ok("{}"));
    }

    private PagoPresencial nuevoPagoPresencial(int variante) {
        return switch (variante) {
            case 1 -> new PagoPresencial(null, 5L, 899990, LocalDateTime.of(2026, 10, 6, 10, 35), EstadoPago.APROBADO, MedioPago.EFECTIVO);
            case 2 -> new PagoPresencial(null, 6L, 79990, LocalDateTime.of(2026, 10, 6, 16, 0), EstadoPago.PENDIENTE, MedioPago.DEBITO);
            default -> new PagoPresencial(null, 5L, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), EstadoPago.REEMBOLSADO, MedioPago.CREDITO);
        };
    }

    @Test
    void crearYListarPagosPresenciales() throws Exception {
        mockMvc.perform(post("/api/v1/pagos-presenciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPagoPresencial(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPago").exists())
                .andExpect(jsonPath("$.medioPago").value("EFECTIVO"));

        mockMvc.perform(post("/api/v1/pagos-presenciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPagoPresencial(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/pagos-presenciales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pagoPresencialList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/pagos-presenciales/idPedido/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pagoPresencialList", hasSize(1)));
    }

    @Test
    void actualizarPagoPresencial() throws Exception {
        PagoPresencial guardado = pagoPresencialRepository.save(nuevoPagoPresencial(1));

        mockMvc.perform(put("/api/v1/pagos-presenciales/" + guardado.getIdPago())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPagoPresencial(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(guardado.getIdPago().intValue()))
                .andExpect(jsonPath("$.medioPago").value("CREDITO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosPresenciales.href").exists());
    }

    @Test
    void crearPagoPresencialConPedidoNoExistente() throws Exception {
        when(restTemplate.getForEntity(any(URI.class), eq(String.class)))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        mockMvc.perform(post("/api/v1/pagos-presenciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoPagoPresencial(1))))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/pagos-presenciales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        PagoPresencial guardado = pagoPresencialRepository.save(nuevoPagoPresencial(1));

        mockMvc.perform(delete("/api/v1/pagos-presenciales/" + guardado.getIdPago()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/pagos-presenciales/" + guardado.getIdPago()))
                .andExpect(status().isNotFound());
    }
}
