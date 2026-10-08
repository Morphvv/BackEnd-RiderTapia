package com.ridertapia.pagos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ridertapia.pagos.model.PagoPresencial;
import com.ridertapia.pagos.model.EstadoPago;
import com.ridertapia.pagos.model.MedioPago;
import com.ridertapia.pagos.service.PagoPresencialService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(PagoPresencialController.class)
@ActiveProfiles("test")
class PagoPresencialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoPresencialService pagoPresencialService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private PagoPresencial pagoPresencial;

    @BeforeEach
    void setUp() {
        pagoPresencial = new PagoPresencial(1L, 5L, 899990, LocalDateTime.of(2026, 10, 6, 10, 35), EstadoPago.APROBADO, MedioPago.EFECTIVO);
    }

    @Test
    void listarPagosPresenciales() throws Exception {
        PagoPresencial otro = new PagoPresencial(2L, 6L, 79990, LocalDateTime.of(2026, 10, 6, 16, 0), EstadoPago.PENDIENTE, MedioPago.DEBITO);
        when(pagoPresencialService.listarPagosPresenciales()).thenReturn(Arrays.asList(pagoPresencial, otro));

        mockMvc.perform(get("/api/v1/pagos-presenciales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pagoPresencialList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.pagoPresencialList[0].idPedido").value(5))
                .andExpect(jsonPath("$._embedded.pagoPresencialList[1].idPedido").value(6))
                .andExpect(jsonPath("$._embedded.pagoPresencialList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(pagoPresencialService.findById(1L)).thenReturn(Optional.of(pagoPresencial));

        mockMvc.perform(get("/api/v1/pagos-presenciales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.idPedido").value(5))
                .andExpect(jsonPath("$.monto").value(899990))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.medioPago").value("EFECTIVO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosPresenciales.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(pagoPresencialService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/pagos-presenciales/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorIdPedidoConResultados() throws Exception {
        when(pagoPresencialService.findByIdPedido(5L)).thenReturn(List.of(pagoPresencial));

        mockMvc.perform(get("/api/v1/pagos-presenciales/idPedido/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pagoPresencialList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.pagoPresencialList[0].idPago").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdPedidoSinResultados() throws Exception {
        when(pagoPresencialService.findByIdPedido(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/pagos-presenciales/idPedido/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearPagoPresencial() throws Exception {
        when(pagoPresencialService.guardarPagoPresencial(any(PagoPresencial.class))).thenReturn(pagoPresencial);

        mockMvc.perform(post("/api/v1/pagos-presenciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoPresencial)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.medioPago").value("EFECTIVO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosPresenciales.href").exists());

        verify(pagoPresencialService, times(1)).guardarPagoPresencial(any(PagoPresencial.class));
    }

    @Test
    void crearPagoPresencialConRelacionNoExistente() throws Exception {
        when(pagoPresencialService.guardarPagoPresencial(any(PagoPresencial.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/pagos-presenciales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoPresencial)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarPagoPresencialExistente() throws Exception {
        PagoPresencial actualizado = new PagoPresencial(1L, 5L, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), EstadoPago.REEMBOLSADO, MedioPago.CREDITO);
        when(pagoPresencialService.actualizarPagoPresencial(eq(1L), any(PagoPresencial.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/pagos-presenciales/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medioPago").value("CREDITO"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosPresenciales.href").exists());
    }

    @Test
    void actualizarPagoPresencialNoExistente() throws Exception {
        when(pagoPresencialService.actualizarPagoPresencial(eq(99L), any(PagoPresencial.class)))
                .thenThrow(new RuntimeException("PagoPresencial no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/pagos-presenciales/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoPresencial)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarPagoPresencial() throws Exception {
        doNothing().when(pagoPresencialService).eliminarPagoPresencial(1L);

        mockMvc.perform(delete("/api/v1/pagos-presenciales/1"))
                .andExpect(status().isNoContent());

        verify(pagoPresencialService, times(1)).eliminarPagoPresencial(1L);
    }
}
