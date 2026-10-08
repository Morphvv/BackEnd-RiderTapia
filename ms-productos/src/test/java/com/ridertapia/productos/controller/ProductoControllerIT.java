package com.ridertapia.productos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ridertapia.productos.LimpiezaBaseDatos;
import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.MarcaRepository;
import com.ridertapia.productos.repository.ProductoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Categoria categoria;
    private Marca marca;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        categoria = categoriaRepository.save(new Categoria(null, "Bicicletas", "Bicicletas de todo tipo", true, null));
        marca = marcaRepository.save(new Marca(null, "Trek", true));
    }

    private Producto nuevoProducto(int variante) {
        return switch (variante) {
            case 1 -> new Producto(null, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), categoria, marca, new ArrayList<>());
            case 2 -> new Producto(null, "Casco Enduro", "Casco con visera", 79990, EstadoPublicacion.PUBLICADO, false, 0, 5, LocalDateTime.of(2026, 10, 6, 12, 0), categoria, marca, new ArrayList<>());
            default -> new Producto(null, "Bicicleta MTB Aro 29 Pro", "Cuadro de carbono", 1099990, EstadoPublicacion.OCULTO, true, 2, 1, LocalDateTime.of(2026, 10, 7, 9, 0), categoria, marca, new ArrayList<>());
        };
    }

    @Test
    void crearYListarProductos() throws Exception {
        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoProducto(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").exists())
                .andExpect(jsonPath("$.precioNormal").value(899990));

        mockMvc.perform(post("/api/v1/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoProducto(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/productos/categoria/" + categoria.getIdCategoria()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.productoList", hasSize(2)));
    }

    @Test
    void actualizarProducto() throws Exception {
        Producto guardado = productoRepository.save(nuevoProducto(1));

        mockMvc.perform(put("/api/v1/productos/" + guardado.getIdProducto())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoProducto(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value(guardado.getIdProducto().intValue()))
                .andExpect(jsonPath("$.precioNormal").value(1099990))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.productos.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Producto guardado = productoRepository.save(nuevoProducto(1));

        mockMvc.perform(delete("/api/v1/productos/" + guardado.getIdProducto()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/productos/" + guardado.getIdProducto()))
                .andExpect(status().isNotFound());
    }
}
