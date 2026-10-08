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

import com.ridertapia.pagos.model.PagoTransferencia;
import com.ridertapia.pagos.model.EstadoPago;
import com.ridertapia.pagos.service.PagoTransferenciaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(PagoTransferenciaController.class)
@ActiveProfiles("test")
class PagoTransferenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoTransferenciaService pagoTransferenciaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private PagoTransferencia pagoTransferencia;

    @BeforeEach
    void setUp() {
        pagoTransferencia = new PagoTransferencia(1L, 5L, 899990, LocalDateTime.of(2026, 10, 6, 10, 35), EstadoPago.APROBADO, "https://docs.ridertapia.cl/comp-1.pdf", LocalDateTime.of(2026, 10, 7, 23, 59), "Comprobante legible");
    }

    @Test
    void listarPagosTransferencia() throws Exception {
        PagoTransferencia otro = new PagoTransferencia(2L, 6L, 79990, LocalDateTime.of(2026, 10, 6, 16, 0), EstadoPago.PENDIENTE, "https://docs.ridertapia.cl/comp-2.pdf", LocalDateTime.of(2026, 10, 8, 23, 59), "Pendiente de revisión");
        when(pagoTransferenciaService.listarPagosTransferencia()).thenReturn(Arrays.asList(pagoTransferencia, otro));

        mockMvc.perform(get("/api/v1/pagos-transferencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pagoTransferenciaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.pagoTransferenciaList[0].idPedido").value(5))
                .andExpect(jsonPath("$._embedded.pagoTransferenciaList[1].idPedido").value(6))
                .andExpect(jsonPath("$._embedded.pagoTransferenciaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(pagoTransferenciaService.findById(1L)).thenReturn(Optional.of(pagoTransferencia));

        mockMvc.perform(get("/api/v1/pagos-transferencia/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.idPedido").value(5))
                .andExpect(jsonPath("$.monto").value(899990))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.comprobanteUrl").value("https://docs.ridertapia.cl/comp-1.pdf"))
                .andExpect(jsonPath("$.fechaLimite").exists())
                .andExpect(jsonPath("$.observacion").value("Comprobante legible"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosTransferencia.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(pagoTransferenciaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/pagos-transferencia/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorIdPedidoConResultados() throws Exception {
        when(pagoTransferenciaService.findByIdPedido(5L)).thenReturn(List.of(pagoTransferencia));

        mockMvc.perform(get("/api/v1/pagos-transferencia/idPedido/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pagoTransferenciaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.pagoTransferenciaList[0].idPago").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdPedidoSinResultados() throws Exception {
        when(pagoTransferenciaService.findByIdPedido(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/pagos-transferencia/idPedido/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearPagoTransferencia() throws Exception {
        when(pagoTransferenciaService.guardarPagoTransferencia(any(PagoTransferencia.class))).thenReturn(pagoTransferencia);

        mockMvc.perform(post("/api/v1/pagos-transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoTransferencia)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.observacion").value("Comprobante legible"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosTransferencia.href").exists());

        verify(pagoTransferenciaService, times(1)).guardarPagoTransferencia(any(PagoTransferencia.class));
    }

    @Test
    void crearPagoTransferenciaConRelacionNoExistente() throws Exception {
        when(pagoTransferenciaService.guardarPagoTransferencia(any(PagoTransferencia.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/pagos-transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoTransferencia)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarPagoTransferenciaExistente() throws Exception {
        PagoTransferencia actualizado = new PagoTransferencia(1L, 5L, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), EstadoPago.REEMBOLSADO, "https://docs.ridertapia.cl/comp-3.pdf", LocalDateTime.of(2026, 10, 9, 23, 59), "Monto no coincide");
        when(pagoTransferenciaService.actualizarPagoTransferencia(eq(1L), any(PagoTransferencia.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/pagos-transferencia/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.observacion").value("Monto no coincide"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosTransferencia.href").exists());
    }

    @Test
    void actualizarPagoTransferenciaNoExistente() throws Exception {
        when(pagoTransferenciaService.actualizarPagoTransferencia(eq(99L), any(PagoTransferencia.class)))
                .thenThrow(new RuntimeException("PagoTransferencia no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/pagos-transferencia/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoTransferencia)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarPagoTransferencia() throws Exception {
        doNothing().when(pagoTransferenciaService).eliminarPagoTransferencia(1L);

        mockMvc.perform(delete("/api/v1/pagos-transferencia/1"))
                .andExpect(status().isNoContent());

        verify(pagoTransferenciaService, times(1)).eliminarPagoTransferencia(1L);
    }
}
