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
import com.ridertapia.productos.model.AvisoStock;
import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.AvisoStockRepository;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.MarcaRepository;
import com.ridertapia.productos.repository.ProductoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AvisoStockControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private AvisoStockRepository avisoStockRepository;

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

    private AvisoStock nuevoAvisoStock(int variante) {
        return switch (variante) {
            case 1 -> new AvisoStock(null, "cliente@correo.cl", LocalDateTime.of(2026, 10, 6, 10, 0), false, producto);
            case 2 -> new AvisoStock(null, "otro@correo.cl", LocalDateTime.of(2026, 10, 6, 12, 0), false, producto);
            default -> new AvisoStock(null, "nuevo@correo.cl", LocalDateTime.of(2026, 10, 7, 9, 0), true, producto);
        };
    }

    @Test
    void crearYListarAvisosStock() throws Exception {
        mockMvc.perform(post("/api/v1/avisos-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoAvisoStock(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAvisoStock").exists())
                .andExpect(jsonPath("$.correo").value("cliente@correo.cl"));

        mockMvc.perform(post("/api/v1/avisos-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoAvisoStock(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/avisos-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.avisoStockList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/avisos-stock/producto/" + producto.getIdProducto()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.avisoStockList", hasSize(2)));
    }

    @Test
    void actualizarAvisoStock() throws Exception {
        AvisoStock guardado = avisoStockRepository.save(nuevoAvisoStock(1));

        mockMvc.perform(put("/api/v1/avisos-stock/" + guardado.getIdAvisoStock())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoAvisoStock(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAvisoStock").value(guardado.getIdAvisoStock().intValue()))
                .andExpect(jsonPath("$.correo").value("nuevo@correo.cl"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.avisosStock.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        AvisoStock guardado = avisoStockRepository.save(nuevoAvisoStock(1));

        mockMvc.perform(delete("/api/v1/avisos-stock/" + guardado.getIdAvisoStock()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/avisos-stock/" + guardado.getIdAvisoStock()))
                .andExpect(status().isNotFound());
    }
}
