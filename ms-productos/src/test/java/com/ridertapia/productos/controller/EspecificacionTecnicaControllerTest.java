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
import com.ridertapia.productos.model.EspecificacionTecnica;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.service.EspecificacionTecnicaService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(EspecificacionTecnicaController.class)
@ActiveProfiles("test")
class EspecificacionTecnicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EspecificacionTecnicaService especificacionTecnicaService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private EspecificacionTecnica especificacionTecnica;

    @BeforeEach
    void setUp() {
        especificacionTecnica = new EspecificacionTecnica(1L, "Material del cuadro", "Aluminio", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void listarEspecificacionesTecnicas() throws Exception {
        EspecificacionTecnica otro = new EspecificacionTecnica(2L, "Peso", "14,5 kg", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(especificacionTecnicaService.listarEspecificacionesTecnicas()).thenReturn(Arrays.asList(especificacionTecnica, otro));

        mockMvc.perform(get("/api/v1/especificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.especificacionTecnicaList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.especificacionTecnicaList[0].nombre").value("Material del cuadro"))
                .andExpect(jsonPath("$._embedded.especificacionTecnicaList[1].nombre").value("Peso"))
                .andExpect(jsonPath("$._embedded.especificacionTecnicaList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(especificacionTecnicaService.findById(1L)).thenReturn(Optional.of(especificacionTecnica));

        mockMvc.perform(get("/api/v1/especificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEspecificacionTecnica").value(1))
                .andExpect(jsonPath("$.nombre").value("Material del cuadro"))
                .andExpect(jsonPath("$.valor").value("Aluminio"))
                .andExpect(jsonPath("$.producto.idProducto").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.especificaciones.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(especificacionTecnicaService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/especificaciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorProductoConResultados() throws Exception {
        when(especificacionTecnicaService.findByProducto(1L)).thenReturn(List.of(especificacionTecnica));

        mockMvc.perform(get("/api/v1/especificaciones/producto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.especificacionTecnicaList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.especificacionTecnicaList[0].idEspecificacionTecnica").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorProductoSinResultados() throws Exception {
        when(especificacionTecnicaService.findByProducto(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/especificaciones/producto/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearEspecificacionTecnica() throws Exception {
        when(especificacionTecnicaService.guardarEspecificacionTecnica(any(EspecificacionTecnica.class))).thenReturn(especificacionTecnica);

        mockMvc.perform(post("/api/v1/especificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(especificacionTecnica)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idEspecificacionTecnica").value(1))
                .andExpect(jsonPath("$.valor").value("Aluminio"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.especificaciones.href").exists());

        verify(especificacionTecnicaService, times(1)).guardarEspecificacionTecnica(any(EspecificacionTecnica.class));
    }

    @Test
    void crearEspecificacionTecnicaConRelacionNoExistente() throws Exception {
        when(especificacionTecnicaService.guardarEspecificacionTecnica(any(EspecificacionTecnica.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/especificaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(especificacionTecnica)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarEspecificacionTecnicaExistente() throws Exception {
        EspecificacionTecnica actualizado = new EspecificacionTecnica(1L, "Material del cuadro", "Carbono", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(especificacionTecnicaService.actualizarEspecificacionTecnica(eq(1L), any(EspecificacionTecnica.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/especificaciones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value("Carbono"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.especificaciones.href").exists());
    }

    @Test
    void actualizarEspecificacionTecnicaNoExistente() throws Exception {
        when(especificacionTecnicaService.actualizarEspecificacionTecnica(eq(99L), any(EspecificacionTecnica.class)))
                .thenThrow(new RuntimeException("EspecificacionTecnica no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/especificaciones/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(especificacionTecnica)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarEspecificacionTecnica() throws Exception {
        doNothing().when(especificacionTecnicaService).eliminarEspecificacionTecnica(1L);

        mockMvc.perform(delete("/api/v1/especificaciones/1"))
                .andExpect(status().isNoContent());

        verify(especificacionTecnicaService, times(1)).eliminarEspecificacionTecnica(1L);
    }
}
