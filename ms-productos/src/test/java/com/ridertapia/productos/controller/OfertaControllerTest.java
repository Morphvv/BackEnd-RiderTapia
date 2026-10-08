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

import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Oferta;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.service.OfertaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(OfertaController.class)
@ActiveProfiles("test")
class OfertaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OfertaService ofertaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Oferta oferta;

    @BeforeEach
    void setUp() {
        oferta = new Oferta(1L, 799990, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void listarOfertas() throws Exception {
        Oferta otro = new Oferta(2L, 749990, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(ofertaService.listarOfertas()).thenReturn(Arrays.asList(oferta, otro));

        mockMvc.perform(get("/api/v1/ofertas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ofertaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.ofertaList[0].precioOferta").value(799990))
                .andExpect(jsonPath("$._embedded.ofertaList[1].precioOferta").value(749990))
                .andExpect(jsonPath("$._embedded.ofertaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(ofertaService.findById(1L)).thenReturn(Optional.of(oferta));

        mockMvc.perform(get("/api/v1/ofertas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idOferta").value(1))
                .andExpect(jsonPath("$.precioOferta").value(799990))
                .andExpect(jsonPath("$.fechaInicio").exists())
                .andExpect(jsonPath("$.fechaTermino").exists())
                .andExpect(jsonPath("$.producto.idProducto").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ofertas.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(ofertaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/ofertas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorProductoConResultados() throws Exception {
        when(ofertaService.findByProducto(1L)).thenReturn(List.of(oferta));

        mockMvc.perform(get("/api/v1/ofertas/producto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.ofertaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.ofertaList[0].idOferta").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorProductoSinResultados() throws Exception {
        when(ofertaService.findByProducto(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ofertas/producto/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearOferta() throws Exception {
        when(ofertaService.guardarOferta(any(Oferta.class))).thenReturn(oferta);

        mockMvc.perform(post("/api/v1/ofertas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oferta)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOferta").value(1))
                .andExpect(jsonPath("$.precioOferta").value(799990))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ofertas.href").exists());

        verify(ofertaService, times(1)).guardarOferta(any(Oferta.class));
    }

    @Test
    void crearOfertaConRelacionNoExistente() throws Exception {
        when(ofertaService.guardarOferta(any(Oferta.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/ofertas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oferta)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarOfertaExistente() throws Exception {
        Oferta actualizado = new Oferta(1L, 699990, LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 30), new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(ofertaService.actualizarOferta(eq(1L), any(Oferta.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/ofertas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioOferta").value(699990))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.ofertas.href").exists());
    }

    @Test
    void actualizarOfertaNoExistente() throws Exception {
        when(ofertaService.actualizarOferta(eq(99L), any(Oferta.class)))
                .thenThrow(new RuntimeException("Oferta no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/ofertas/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(oferta)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarOferta() throws Exception {
        doNothing().when(ofertaService).eliminarOferta(1L);

        mockMvc.perform(delete("/api/v1/ofertas/1"))
                .andExpect(status().isNoContent());

        verify(ofertaService, times(1)).eliminarOferta(1L);
    }
}
