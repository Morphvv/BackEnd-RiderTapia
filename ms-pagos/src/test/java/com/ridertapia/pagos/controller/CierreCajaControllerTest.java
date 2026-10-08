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

import java.time.LocalDate;
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

import com.ridertapia.pagos.model.CierreCaja;

import com.ridertapia.pagos.service.CierreCajaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(CierreCajaController.class)
@ActiveProfiles("test")
class CierreCajaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CierreCajaService cierreCajaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private CierreCaja cierreCaja;

    @BeforeEach
    void setUp() {
        cierreCaja = new CierreCaja(1L, LocalDate.of(2026, 10, 6), 250000, 420000, 899990, 79990, 250000, 0, 1L);
    }

    @Test
    void listarCierresCaja() throws Exception {
        CierreCaja otro = new CierreCaja(2L, LocalDate.of(2026, 10, 7), 180000, 310000, 0, 120000, 175000, -5000, 2L);
        when(cierreCajaService.listarCierresCaja()).thenReturn(Arrays.asList(cierreCaja, otro));

        mockMvc.perform(get("/api/v1/cierres-caja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cierreCajaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.cierreCajaList[0].fecha").exists())
                .andExpect(jsonPath("$._embedded.cierreCajaList[1].fecha").exists())
                .andExpect(jsonPath("$._embedded.cierreCajaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(cierreCajaService.findById(1L)).thenReturn(Optional.of(cierreCaja));

        mockMvc.perform(get("/api/v1/cierres-caja/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCierreCaja").value(1))
                .andExpect(jsonPath("$.fecha").exists())
                .andExpect(jsonPath("$.totalEfectivo").value(250000))
                .andExpect(jsonPath("$.totalDebito").value(420000))
                .andExpect(jsonPath("$.totalCredito").value(899990))
                .andExpect(jsonPath("$.totalTransferencia").value(79990))
                .andExpect(jsonPath("$.efectivoContado").value(250000))
                .andExpect(jsonPath("$.diferencia").value(0))
                .andExpect(jsonPath("$.idEmpleado").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cierresCaja.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(cierreCajaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/cierres-caja/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorIdEmpleadoConResultados() throws Exception {
        when(cierreCajaService.findByIdEmpleado(1L)).thenReturn(List.of(cierreCaja));

        mockMvc.perform(get("/api/v1/cierres-caja/idEmpleado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cierreCajaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.cierreCajaList[0].idCierreCaja").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdEmpleadoSinResultados() throws Exception {
        when(cierreCajaService.findByIdEmpleado(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cierres-caja/idEmpleado/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearCierreCaja() throws Exception {
        when(cierreCajaService.guardarCierreCaja(any(CierreCaja.class))).thenReturn(cierreCaja);

        mockMvc.perform(post("/api/v1/cierres-caja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cierreCaja)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCierreCaja").value(1))
                .andExpect(jsonPath("$.efectivoContado").value(250000))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cierresCaja.href").exists());

        verify(cierreCajaService, times(1)).guardarCierreCaja(any(CierreCaja.class));
    }

    @Test
    void actualizarCierreCajaExistente() throws Exception {
        CierreCaja actualizado = new CierreCaja(1L, LocalDate.of(2026, 10, 8), 250000, 420000, 899990, 79990, 249000, -1000, 1L);
        when(cierreCajaService.actualizarCierreCaja(eq(1L), any(CierreCaja.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/cierres-caja/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.efectivoContado").value(249000))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cierresCaja.href").exists());
    }

    @Test
    void actualizarCierreCajaNoExistente() throws Exception {
        when(cierreCajaService.actualizarCierreCaja(eq(99L), any(CierreCaja.class)))
                .thenThrow(new RuntimeException("CierreCaja no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/cierres-caja/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cierreCaja)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarCierreCaja() throws Exception {
        doNothing().when(cierreCajaService).eliminarCierreCaja(1L);

        mockMvc.perform(delete("/api/v1/cierres-caja/1"))
                .andExpect(status().isNoContent());

        verify(cierreCajaService, times(1)).eliminarCierreCaja(1L);
    }
}
