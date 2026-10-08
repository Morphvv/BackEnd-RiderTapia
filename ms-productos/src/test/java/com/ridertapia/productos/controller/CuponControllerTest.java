package com.ridertapia.productos.controller;

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

import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Cupon;
import com.ridertapia.productos.model.TipoDescuento;
import com.ridertapia.productos.service.CuponService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(CuponController.class)
@ActiveProfiles("test")
class CuponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuponService cuponService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Cupon cupon;

    @BeforeEach
    void setUp() {
        cupon = new Cupon(1L, "RIDER10", TipoDescuento.PORCENTAJE, 10, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 100, 0, true, new ArrayList<>());
    }

    @Test
    void listarCupones() throws Exception {
        Cupon otro = new Cupon(2L, "BIENVENIDA5000", TipoDescuento.MONTO_FIJO, 5000, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 50, 0, true, new ArrayList<>());
        when(cuponService.listarCupones()).thenReturn(Arrays.asList(cupon, otro));

        mockMvc.perform(get("/api/v1/cupones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cuponList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.cuponList[0].codigo").value("RIDER10"))
                .andExpect(jsonPath("$._embedded.cuponList[1].codigo").value("BIENVENIDA5000"))
                .andExpect(jsonPath("$._embedded.cuponList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(cuponService.findById(1L)).thenReturn(Optional.of(cupon));

        mockMvc.perform(get("/api/v1/cupones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCupon").value(1))
                .andExpect(jsonPath("$.codigo").value("RIDER10"))
                .andExpect(jsonPath("$.tipoDescuento").value("PORCENTAJE"))
                .andExpect(jsonPath("$.valor").value(10))
                .andExpect(jsonPath("$.fechaInicio").exists())
                .andExpect(jsonPath("$.fechaTermino").exists())
                .andExpect(jsonPath("$.limiteUsos").value(100))
                .andExpect(jsonPath("$.usosActuales").value(0))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.categorias").isArray())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cupones.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(cuponService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/cupones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorCodigoExistente() throws Exception {
        when(cuponService.findByCodigo("RIDER10")).thenReturn(cupon);

        mockMvc.perform(get("/api/v1/cupones/codigo/RIDER10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCupon").value(1))
                .andExpect(jsonPath("$.codigo").value("RIDER10"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cupones.href").exists());
    }

    @Test
    void obtenerPorCodigoNoExistente() throws Exception {
        when(cuponService.findByCodigo("NO-EXISTE")).thenReturn(null);

        mockMvc.perform(get("/api/v1/cupones/codigo/NO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearCupon() throws Exception {
        when(cuponService.guardarCupon(any(Cupon.class))).thenReturn(cupon);

        mockMvc.perform(post("/api/v1/cupones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cupon)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCupon").value(1))
                .andExpect(jsonPath("$.valor").value(10))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cupones.href").exists());

        verify(cuponService, times(1)).guardarCupon(any(Cupon.class));
    }

    @Test
    void crearCuponConRelacionNoExistente() throws Exception {
        when(cuponService.guardarCupon(any(Cupon.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/cupones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cupon)))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearCuponDuplicado() throws Exception {
        when(cuponService.guardarCupon(any(Cupon.class)))
                .thenThrow(new DataIntegrityViolationException("valor duplicado"));

        mockMvc.perform(post("/api/v1/cupones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cupon)))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarCuponExistente() throws Exception {
        Cupon actualizado = new Cupon(1L, "RIDER15", TipoDescuento.PORCENTAJE, 15, LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 30), 200, 10, false, new ArrayList<>());
        when(cuponService.actualizarCupon(eq(1L), any(Cupon.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/cupones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(15))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cupones.href").exists());
    }

    @Test
    void actualizarCuponNoExistente() throws Exception {
        when(cuponService.actualizarCupon(eq(99L), any(Cupon.class)))
                .thenThrow(new RuntimeException("Cupon no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/cupones/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cupon)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarCupon() throws Exception {
        doNothing().when(cuponService).eliminarCupon(1L);

        mockMvc.perform(delete("/api/v1/cupones/1"))
                .andExpect(status().isNoContent());

        verify(cuponService, times(1)).eliminarCupon(1L);
    }
}
