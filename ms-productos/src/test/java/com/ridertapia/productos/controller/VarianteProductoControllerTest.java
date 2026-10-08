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
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.VarianteProducto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.service.VarianteProductoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(VarianteProductoController.class)
@ActiveProfiles("test")
class VarianteProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VarianteProductoService varianteProductoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private VarianteProducto varianteProducto;

    @BeforeEach
    void setUp() {
        varianteProducto = new VarianteProducto(1L, "BIC-MTB29-M-ROJ", "M", "Rojo", "29", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void listarVariantesProducto() throws Exception {
        VarianteProducto otro = new VarianteProducto(2L, "BIC-MTB29-L-NEG", "L", "Negro", "29", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(varianteProductoService.listarVariantesProducto()).thenReturn(Arrays.asList(varianteProducto, otro));

        mockMvc.perform(get("/api/v1/variantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.varianteProductoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.varianteProductoList[0].sku").value("BIC-MTB29-M-ROJ"))
                .andExpect(jsonPath("$._embedded.varianteProductoList[1].sku").value("BIC-MTB29-L-NEG"))
                .andExpect(jsonPath("$._embedded.varianteProductoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(varianteProductoService.findById(1L)).thenReturn(Optional.of(varianteProducto));

        mockMvc.perform(get("/api/v1/variantes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVarianteProducto").value(1))
                .andExpect(jsonPath("$.sku").value("BIC-MTB29-M-ROJ"))
                .andExpect(jsonPath("$.talla").value("M"))
                .andExpect(jsonPath("$.color").value("Rojo"))
                .andExpect(jsonPath("$.aro").value("29"))
                .andExpect(jsonPath("$.producto.idProducto").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.variantes.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(varianteProductoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/variantes/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorSkuExistente() throws Exception {
        when(varianteProductoService.findBySku("BIC-MTB29-M-ROJ")).thenReturn(varianteProducto);

        mockMvc.perform(get("/api/v1/variantes/sku/BIC-MTB29-M-ROJ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVarianteProducto").value(1))
                .andExpect(jsonPath("$.sku").value("BIC-MTB29-M-ROJ"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.variantes.href").exists());
    }

    @Test
    void obtenerPorSkuNoExistente() throws Exception {
        when(varianteProductoService.findBySku("NO-EXISTE")).thenReturn(null);

        mockMvc.perform(get("/api/v1/variantes/sku/NO-EXISTE"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearVarianteProducto() throws Exception {
        when(varianteProductoService.guardarVarianteProducto(any(VarianteProducto.class))).thenReturn(varianteProducto);

        mockMvc.perform(post("/api/v1/variantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(varianteProducto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idVarianteProducto").value(1))
                .andExpect(jsonPath("$.color").value("Rojo"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.variantes.href").exists());

        verify(varianteProductoService, times(1)).guardarVarianteProducto(any(VarianteProducto.class));
    }

    @Test
    void crearVarianteProductoConRelacionNoExistente() throws Exception {
        when(varianteProductoService.guardarVarianteProducto(any(VarianteProducto.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/variantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(varianteProducto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearVarianteProductoDuplicado() throws Exception {
        when(varianteProductoService.guardarVarianteProducto(any(VarianteProducto.class)))
                .thenThrow(new DataIntegrityViolationException("valor duplicado"));

        mockMvc.perform(post("/api/v1/variantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(varianteProducto)))
                .andExpect(status().isConflict());
    }

    @Test
    void actualizarVarianteProductoExistente() throws Exception {
        VarianteProducto actualizado = new VarianteProducto(1L, "BIC-MTB29-M-AZU", "M", "Azul", "27.5", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(varianteProductoService.actualizarVarianteProducto(eq(1L), any(VarianteProducto.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/variantes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("Azul"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.variantes.href").exists());
    }

    @Test
    void actualizarVarianteProductoNoExistente() throws Exception {
        when(varianteProductoService.actualizarVarianteProducto(eq(99L), any(VarianteProducto.class)))
                .thenThrow(new RuntimeException("VarianteProducto no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/variantes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(varianteProducto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarVarianteProducto() throws Exception {
        doNothing().when(varianteProductoService).eliminarVarianteProducto(1L);

        mockMvc.perform(delete("/api/v1/variantes/1"))
                .andExpect(status().isNoContent());

        verify(varianteProductoService, times(1)).eliminarVarianteProducto(1L);
    }
}
