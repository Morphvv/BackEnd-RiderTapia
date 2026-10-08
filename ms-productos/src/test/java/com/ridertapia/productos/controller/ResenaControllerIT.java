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
import com.ridertapia.productos.model.Resena;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.MarcaRepository;
import com.ridertapia.productos.repository.ProductoRepository;
import com.ridertapia.productos.repository.ResenaRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ResenaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private ResenaRepository resenaRepository;

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

    private Resena nuevoResena(int variante) {
        return switch (variante) {
            case 1 -> new Resena(null, 3L, 5, "Excelente bicicleta", LocalDateTime.of(2026, 10, 6, 10, 0), true, "Gracias por tu compra", producto);
            case 2 -> new Resena(null, 4L, 4, "Buena relación precio calidad", LocalDateTime.of(2026, 10, 6, 12, 0), true, "Gracias por comentar", producto);
            default -> new Resena(null, 3L, 3, "Cambié de opinión", LocalDateTime.of(2026, 10, 7, 9, 0), false, "Lamentamos tu experiencia", producto);
        };
    }

    @Test
    void crearYListarResenas() throws Exception {
        mockMvc.perform(post("/api/v1/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoResena(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idResena").exists())
                .andExpect(jsonPath("$.calificacion").value(5));

        mockMvc.perform(post("/api/v1/resenas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoResena(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.resenaList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/resenas/producto/" + producto.getIdProducto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.resenaList", hasSize(2)));
    }

    @Test
    void actualizarResena() throws Exception {
        Resena guardado = resenaRepository.save(nuevoResena(1));

        mockMvc.perform(put("/api/v1/resenas/" + guardado.getIdResena())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoResena(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idResena").value(guardado.getIdResena().intValue()))
                .andExpect(jsonPath("$.calificacion").value(3))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.resenas.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Resena guardado = resenaRepository.save(nuevoResena(1));

        mockMvc.perform(delete("/api/v1/resenas/" + guardado.getIdResena()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/resenas/" + guardado.getIdResena()))
                .andExpect(status().isNotFound());
    }
}
