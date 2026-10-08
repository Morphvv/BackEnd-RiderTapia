package com.ridertapia.carrito.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ridertapia.carrito.dto.AvisoCambio;
import com.ridertapia.carrito.dto.ResultadoValidacionCupon;
import com.ridertapia.carrito.dto.ResumenCarrito;
import com.ridertapia.carrito.model.TipoAviso;
import com.ridertapia.carrito.service.CarritoService;

// Tests de los endpoints de resumen, avisos y validación de cupón
@WebMvcTest(CarritoController.class)
@ActiveProfiles("test")
class CarritoOperacionesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    @Test
    void obtenerResumen() throws Exception {
        when(carritoService.obtenerResumen(1L)).thenReturn(new ResumenCarrito(3, 979980));

        mockMvc.perform(get("/api/v1/carritos/1/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadItems").value(3))
                .andExpect(jsonPath("$.total").value(979980))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.carrito.href").exists());
    }

    @Test
    void obtenerResumenCarritoNoExistente() throws Exception {
        when(carritoService.obtenerResumen(99L)).thenThrow(new RuntimeException("Carrito no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/carritos/99/resumen"))
                .andExpect(status().isNotFound());
    }

    @Test
    void revalidarConAvisos() throws Exception {
        when(carritoService.revalidar(1L)).thenReturn(List.of(
                new AvisoCambio(TipoAviso.SIN_STOCK, "El producto ya no tiene stock", 899990, 899990, 0, 1L)));

        mockMvc.perform(get("/api/v1/carritos/1/avisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.avisoCambioList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.avisoCambioList[0].tipo").value("SIN_STOCK"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void revalidarSinAvisos() throws Exception {
        when(carritoService.revalidar(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/carritos/1/avisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void revalidarCarritoNoExistente() throws Exception {
        when(carritoService.revalidar(99L)).thenThrow(new RuntimeException("Carrito no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/carritos/99/avisos"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validarCupon() throws Exception {
        when(carritoService.validarCupon(1L, "RIDER10"))
                .thenReturn(new ResultadoValidacionCupon(true, "Cupón válido", 97998));

        mockMvc.perform(get("/api/v1/carritos/1/cupon/RIDER10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(jsonPath("$.montoDescuento").value(97998))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void validarCuponCarritoNoExistente() throws Exception {
        when(carritoService.validarCupon(99L, "RIDER10"))
                .thenThrow(new RuntimeException("Carrito no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/carritos/99/cupon/RIDER10"))
                .andExpect(status().isNotFound());
    }
}
