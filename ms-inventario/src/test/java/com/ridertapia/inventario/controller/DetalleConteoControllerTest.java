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

import com.ridertapia.inventario.model.DetalleConteo;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.model.EstadoToma;
import com.ridertapia.inventario.service.DetalleConteoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(DetalleConteoController.class)
@ActiveProfiles("test")
class DetalleConteoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DetalleConteoService detalleConteoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private DetalleConteo detalleConteo;

    @BeforeEach
    void setUp() {
        detalleConteo = new DetalleConteo(1L, 15, 14, -1, new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L), new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    @Test
    void listarDetallesConteo() throws Exception {
        DetalleConteo otro = new DetalleConteo(2L, 8, 8, 0, new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L), new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(detalleConteoService.listarDetallesConteo()).thenReturn(Arrays.asList(detalleConteo, otro));

        mockMvc.perform(get("/api/v1/detalles-conteo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.detalleConteoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.detalleConteoList[0].cantidadSistema").value(15))
                .andExpect(jsonPath("$._embedded.detalleConteoList[1].cantidadSistema").value(8))
                .andExpect(jsonPath("$._embedded.detalleConteoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(detalleConteoService.findById(1L)).thenReturn(Optional.of(detalleConteo));

        mockMvc.perform(get("/api/v1/detalles-conteo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDetalleConteo").value(1))
                .andExpect(jsonPath("$.cantidadSistema").value(15))
                .andExpect(jsonPath("$.cantidadContada").value(14))
                .andExpect(jsonPath("$.diferencia").value(-1))
                .andExpect(jsonPath("$.tomaInventario.idTomaInventario").value(1))
                .andExpect(jsonPath("$.itemInventario.idItemInventario").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.detallesConteo.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(detalleConteoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/detalles-conteo/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorTomaInventarioConResultados() throws Exception {
        when(detalleConteoService.findByTomaInventario(1L)).thenReturn(List.of(detalleConteo));

        mockMvc.perform(get("/api/v1/detalles-conteo/tomaInventario/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.detalleConteoList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.detalleConteoList[0].idDetalleConteo").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorTomaInventarioSinResultados() throws Exception {
        when(detalleConteoService.findByTomaInventario(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/detalles-conteo/tomaInventario/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearDetalleConteo() throws Exception {
        when(detalleConteoService.guardarDetalleConteo(any(DetalleConteo.class))).thenReturn(detalleConteo);

        mockMvc.perform(post("/api/v1/detalles-conteo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detalleConteo)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDetalleConteo").value(1))
                .andExpect(jsonPath("$.cantidadContada").value(14))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.detallesConteo.href").exists());

        verify(detalleConteoService, times(1)).guardarDetalleConteo(any(DetalleConteo.class));
    }

    @Test
    void crearDetalleConteoConRelacionNoExistente() throws Exception {
        when(detalleConteoService.guardarDetalleConteo(any(DetalleConteo.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/detalles-conteo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detalleConteo)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarDetalleConteoExistente() throws Exception {
        DetalleConteo actualizado = new DetalleConteo(1L, 15, 12, -3, new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L), new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(detalleConteoService.actualizarDetalleConteo(eq(1L), any(DetalleConteo.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/detalles-conteo/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadContada").value(12))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.detallesConteo.href").exists());
    }

    @Test
    void actualizarDetalleConteoNoExistente() throws Exception {
        when(detalleConteoService.actualizarDetalleConteo(eq(99L), any(DetalleConteo.class)))
                .thenThrow(new RuntimeException("DetalleConteo no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/detalles-conteo/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detalleConteo)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarDetalleConteo() throws Exception {
        doNothing().when(detalleConteoService).eliminarDetalleConteo(1L);

        mockMvc.perform(delete("/api/v1/detalles-conteo/1"))
                .andExpect(status().isNoContent());

        verify(detalleConteoService, times(1)).eliminarDetalleConteo(1L);
    }
}
