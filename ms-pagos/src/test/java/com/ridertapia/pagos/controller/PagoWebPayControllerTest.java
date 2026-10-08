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

import com.ridertapia.pagos.model.PagoWebPay;
import com.ridertapia.pagos.model.EstadoPago;
import com.ridertapia.pagos.service.PagoWebPayService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(PagoWebPayController.class)
@ActiveProfiles("test")
class PagoWebPayControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoWebPayService pagoWebPayService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private PagoWebPay pagoWebPay;

    @BeforeEach
    void setUp() {
        pagoWebPay = new PagoWebPay(1L, 5L, 899990, LocalDateTime.of(2026, 10, 6, 10, 35), EstadoPago.APROBADO, "01ab7c9e2f", "AUT-123456", "4417");
    }

    @Test
    void listarPagosWebPay() throws Exception {
        PagoWebPay otro = new PagoWebPay(2L, 6L, 79990, LocalDateTime.of(2026, 10, 6, 16, 0), EstadoPago.PENDIENTE, "02cd8d0f3a", "AUT-654321", "1234");
        when(pagoWebPayService.listarPagosWebPay()).thenReturn(Arrays.asList(pagoWebPay, otro));

        mockMvc.perform(get("/api/v1/pagos-webpay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pagoWebPayList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.pagoWebPayList[0].idPedido").value(5))
                .andExpect(jsonPath("$._embedded.pagoWebPayList[1].idPedido").value(6))
                .andExpect(jsonPath("$._embedded.pagoWebPayList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(pagoWebPayService.findById(1L)).thenReturn(Optional.of(pagoWebPay));

        mockMvc.perform(get("/api/v1/pagos-webpay/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.idPedido").value(5))
                .andExpect(jsonPath("$.monto").value(899990))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.estado").value("APROBADO"))
                .andExpect(jsonPath("$.token").value("01ab7c9e2f"))
                .andExpect(jsonPath("$.codigoAutorizacion").value("AUT-123456"))
                .andExpect(jsonPath("$.ultimosDigitosTarjeta").value("4417"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosWebpay.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(pagoWebPayService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/pagos-webpay/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorIdPedidoConResultados() throws Exception {
        when(pagoWebPayService.findByIdPedido(5L)).thenReturn(List.of(pagoWebPay));

        mockMvc.perform(get("/api/v1/pagos-webpay/idPedido/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pagoWebPayList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.pagoWebPayList[0].idPago").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdPedidoSinResultados() throws Exception {
        when(pagoWebPayService.findByIdPedido(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/pagos-webpay/idPedido/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearPagoWebPay() throws Exception {
        when(pagoWebPayService.guardarPagoWebPay(any(PagoWebPay.class))).thenReturn(pagoWebPay);

        mockMvc.perform(post("/api/v1/pagos-webpay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoWebPay)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPago").value(1))
                .andExpect(jsonPath("$.codigoAutorizacion").value("AUT-123456"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosWebpay.href").exists());

        verify(pagoWebPayService, times(1)).guardarPagoWebPay(any(PagoWebPay.class));
    }

    @Test
    void crearPagoWebPayConRelacionNoExistente() throws Exception {
        when(pagoWebPayService.guardarPagoWebPay(any(PagoWebPay.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/pagos-webpay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoWebPay)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarPagoWebPayExistente() throws Exception {
        PagoWebPay actualizado = new PagoWebPay(1L, 5L, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), EstadoPago.REEMBOLSADO, "03ef9e1a4b", "AUT-777777", "4417");
        when(pagoWebPayService.actualizarPagoWebPay(eq(1L), any(PagoWebPay.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/pagos-webpay/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoAutorizacion").value("AUT-777777"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.pagosWebpay.href").exists());
    }

    @Test
    void actualizarPagoWebPayNoExistente() throws Exception {
        when(pagoWebPayService.actualizarPagoWebPay(eq(99L), any(PagoWebPay.class)))
                .thenThrow(new RuntimeException("PagoWebPay no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/pagos-webpay/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoWebPay)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarPagoWebPay() throws Exception {
        doNothing().when(pagoWebPayService).eliminarPagoWebPay(1L);

        mockMvc.perform(delete("/api/v1/pagos-webpay/1"))
                .andExpect(status().isNoContent());

        verify(pagoWebPayService, times(1)).eliminarPagoWebPay(1L);
    }
}
