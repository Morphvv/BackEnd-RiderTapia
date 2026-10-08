package com.ridertapia.inventario.controller;

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

import com.ridertapia.inventario.model.CargaMasiva;
import com.ridertapia.inventario.model.ErrorCargaMasiva;

import com.ridertapia.inventario.service.ErrorCargaMasivaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(ErrorCargaMasivaController.class)
@ActiveProfiles("test")
class ErrorCargaMasivaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ErrorCargaMasivaService errorCargaMasivaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private ErrorCargaMasiva errorCargaMasiva;

    @BeforeEach
    void setUp() {
        errorCargaMasiva = new ErrorCargaMasiva(1L, 15, "SKU repetido", new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L));
    }

    @Test
    void listarErroresCargaMasiva() throws Exception {
        ErrorCargaMasiva otro = new ErrorCargaMasiva(2L, 87, "Precio vacío", new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L));
        when(errorCargaMasivaService.listarErroresCargaMasiva()).thenReturn(Arrays.asList(errorCargaMasiva, otro));

        mockMvc.perform(get("/api/v1/errores-carga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.errorCargaMasivaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.errorCargaMasivaList[0].fila").value(15))
                .andExpect(jsonPath("$._embedded.errorCargaMasivaList[1].fila").value(87))
                .andExpect(jsonPath("$._embedded.errorCargaMasivaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(errorCargaMasivaService.findById(1L)).thenReturn(Optional.of(errorCargaMasiva));

        mockMvc.perform(get("/api/v1/errores-carga/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idErrorCargaMasiva").value(1))
                .andExpect(jsonPath("$.fila").value(15))
                .andExpect(jsonPath("$.mensaje").value("SKU repetido"))
                .andExpect(jsonPath("$.cargaMasiva.idCargaMasiva").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.erroresCarga.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(errorCargaMasivaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/errores-carga/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorCargaMasivaConResultados() throws Exception {
        when(errorCargaMasivaService.findByCargaMasiva(1L)).thenReturn(List.of(errorCargaMasiva));

        mockMvc.perform(get("/api/v1/errores-carga/cargaMasiva/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.errorCargaMasivaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.errorCargaMasivaList[0].idErrorCargaMasiva").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorCargaMasivaSinResultados() throws Exception {
        when(errorCargaMasivaService.findByCargaMasiva(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/errores-carga/cargaMasiva/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearErrorCargaMasiva() throws Exception {
        when(errorCargaMasivaService.guardarErrorCargaMasiva(any(ErrorCargaMasiva.class))).thenReturn(errorCargaMasiva);

        mockMvc.perform(post("/api/v1/errores-carga")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(errorCargaMasiva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idErrorCargaMasiva").value(1))
                .andExpect(jsonPath("$.mensaje").value("SKU repetido"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.erroresCarga.href").exists());

        verify(errorCargaMasivaService, times(1)).guardarErrorCargaMasiva(any(ErrorCargaMasiva.class));
    }

    @Test
    void crearErrorCargaMasivaConRelacionNoExistente() throws Exception {
        when(errorCargaMasivaService.guardarErrorCargaMasiva(any(ErrorCargaMasiva.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/errores-carga")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(errorCargaMasiva)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarErrorCargaMasivaExistente() throws Exception {
        ErrorCargaMasiva actualizado = new ErrorCargaMasiva(1L, 16, "SKU no existe", new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L));
        when(errorCargaMasivaService.actualizarErrorCargaMasiva(eq(1L), any(ErrorCargaMasiva.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/errores-carga/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("SKU no existe"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.erroresCarga.href").exists());
    }

    @Test
    void actualizarErrorCargaMasivaNoExistente() throws Exception {
        when(errorCargaMasivaService.actualizarErrorCargaMasiva(eq(99L), any(ErrorCargaMasiva.class)))
                .thenThrow(new RuntimeException("ErrorCargaMasiva no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/errores-carga/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(errorCargaMasiva)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarErrorCargaMasiva() throws Exception {
        doNothing().when(errorCargaMasivaService).eliminarErrorCargaMasiva(1L);

        mockMvc.perform(delete("/api/v1/errores-carga/1"))
                .andExpect(status().isNoContent());

        verify(errorCargaMasivaService, times(1)).eliminarErrorCargaMasiva(1L);
    }
}
