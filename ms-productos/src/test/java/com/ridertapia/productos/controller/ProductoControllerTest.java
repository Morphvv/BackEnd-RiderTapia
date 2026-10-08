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
import com.ridertapia.productos.model.Compatibilidad;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.service.ProductoService;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(ProductoController.class)
@ActiveProfiles("test")
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductoService productoService;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>());
    }

    @Test
    void listarProductos() throws Exception {
        Producto otro = new Producto(2L, "Casco Enduro", "Casco con visera", 79990, EstadoPublicacion.PUBLICADO, false, 0, 5, LocalDateTime.of(2026, 10, 6, 12, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>());
        when(productoService.listarProductos()).thenReturn(Arrays.asList(producto, otro));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productoList", hasSize(2)))
                .andExpect(jsonPath("$._embedded.productoList[0].nombre").value("Bicicleta MTB Aro 29"))
                .andExpect(jsonPath("$._embedded.productoList[1].nombre").value("Casco Enduro"))
                .andExpect(jsonPath("$._embedded.productoList[0]._links.self.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorIdExistente() throws Exception {
        when(productoService.findById(1L)).thenReturn(Optional.of(producto));

        mockMvc.perform(get("/api/v1/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.nombre").value("Bicicleta MTB Aro 29"))
                .andExpect(jsonPath("$.descripcion").value("Cuadro de aluminio, 12 velocidades"))
                .andExpect(jsonPath("$.precioNormal").value(899990))
                .andExpect(jsonPath("$.estado").value("PUBLICADO"))
                .andExpect(jsonPath("$.destacado").value(true))
                .andExpect(jsonPath("$.ordenDestacado").value(1))
                .andExpect(jsonPath("$.limitePorCompra").value(2))
                .andExpect(jsonPath("$.fechaCreacion").exists())
                .andExpect(jsonPath("$.categoria.idCategoria").value(1))
                .andExpect(jsonPath("$.marca.idMarca").value(1))
                .andExpect(jsonPath("$.compatibilidades").isArray())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.productos.href").exists());
    }

    @Test
    void obtenerPorIdNoExistente() throws Exception {
        when(productoService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPorCategoriaConResultados() throws Exception {
        when(productoService.findByCategoria(1L)).thenReturn(List.of(producto));

        mockMvc.perform(get("/api/v1/productos/categoria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productoList", hasSize(1)))
                .andExpect(jsonPath("$._embedded.productoList[0].idProducto").value(1))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void obtenerPorCategoriaSinResultados() throws Exception {
        when(productoService.findByCategoria(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/productos/categoria/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void crearProducto() throws Exception {
        when(productoService.guardarProducto(any(Producto.class))).thenReturn(producto);

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.precioNormal").value(899990))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.productos.href").exists());

        verify(productoService, times(1)).guardarProducto(any(Producto.class));
    }

    @Test
    void crearProductoConRelacionNoExistente() throws Exception {
        when(productoService.guardarProducto(any(Producto.class)))
                .thenThrow(new RuntimeException("Registro relacionado no encontrado"));

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarProductoExistente() throws Exception {
        Producto actualizado = new Producto(1L, "Bicicleta MTB Aro 29 Pro", "Cuadro de carbono", 1099990, EstadoPublicacion.OCULTO, true, 2, 1, LocalDateTime.of(2026, 10, 7, 9, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>());
        when(productoService.actualizarProducto(eq(1L), any(Producto.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precioNormal").value(1099990))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.productos.href").exists());
    }

    @Test
    void actualizarProductoNoExistente() throws Exception {
        when(productoService.actualizarProducto(eq(99L), any(Producto.class)))
                .thenThrow(new RuntimeException("Producto no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/productos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void eliminarProducto() throws Exception {
        doNothing().when(productoService).eliminarProducto(1L);

        mockMvc.perform(delete("/api/v1/productos/1"))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).eliminarProducto(1L);
    }

    @Test
    void eliminarProductoEnUso() throws Exception {
        doThrow(new DataIntegrityViolationException("en uso")).when(productoService).eliminarProducto(1L);

        mockMvc.perform(delete("/api/v1/productos/1"))
                .andExpect(status().isConflict());
    }
}
