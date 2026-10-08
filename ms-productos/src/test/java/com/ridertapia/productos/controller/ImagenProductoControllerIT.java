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
import com.ridertapia.productos.model.ImagenProducto;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.ImagenProductoRepository;
import com.ridertapia.productos.repository.MarcaRepository;
import com.ridertapia.productos.repository.ProductoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ImagenProductoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private Categoria categoria;
    private Marca marca;
    private Producto producto;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        categoria = categoriaRepository.save(new Categoria(null, "Bicicletas", "Bicicletas de todo tipo", true, null));
        marca = marcaRepository.save(new Marca(null, "Trek", true));
        producto = productoRepository.save(new Producto(null, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), categoria, marca, new ArrayList<>()));
    }

    private ImagenProducto nuevoImagenProducto(int variante) {
        return switch (variante) {
            case 1 -> new ImagenProducto(null, "https://img.ridertapia.cl/mtb29-1.jpg", 1, true, producto);
            case 2 -> new ImagenProducto(null, "https://img.ridertapia.cl/mtb29-2.jpg", 2, false, producto);
            default -> new ImagenProducto(null, "https://img.ridertapia.cl/mtb29-3.jpg", 3, false, producto);
        };
    }

    @Test
    void crearYListarImagenesProducto() throws Exception {
        mockMvc.perform(post("/api/v1/imagenes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoImagenProducto(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idImagenProducto").exists())
                .andExpect(jsonPath("$.url").value("https://img.ridertapia.cl/mtb29-1.jpg"));

        mockMvc.perform(post("/api/v1/imagenes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoImagenProducto(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/imagenes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.imagenProductoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/imagenes/producto/" + producto.getIdProducto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.imagenProductoList", hasSize(2)));
    }

    @Test
    void actualizarImagenProducto() throws Exception {
        ImagenProducto guardado = imagenProductoRepository.save(nuevoImagenProducto(1));

        mockMvc.perform(put("/api/v1/imagenes/" + guardado.getIdImagenProducto())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoImagenProducto(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idImagenProducto").value(guardado.getIdImagenProducto().intValue()))
                .andExpect(jsonPath("$.url").value("https://img.ridertapia.cl/mtb29-3.jpg"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.imagenes.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        ImagenProducto guardado = imagenProductoRepository.save(nuevoImagenProducto(1));

        mockMvc.perform(delete("/api/v1/imagenes/" + guardado.getIdImagenProducto()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/imagenes/" + guardado.getIdImagenProducto()))
                .andExpect(status().isNotFound());
    }
}
