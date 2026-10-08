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
import com.ridertapia.productos.model.VarianteProducto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.MarcaRepository;
import com.ridertapia.productos.repository.ProductoRepository;
import com.ridertapia.productos.repository.VarianteProductoRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VarianteProductoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private VarianteProductoRepository varianteProductoRepository;

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

    private VarianteProducto nuevoVarianteProducto(int variante) {
        return switch (variante) {
            case 1 -> new VarianteProducto(null, "BIC-MTB29-M-ROJ", "M", "Rojo", "29", producto);
            case 2 -> new VarianteProducto(null, "BIC-MTB29-L-NEG", "L", "Negro", "29", producto);
            default -> new VarianteProducto(null, "BIC-MTB29-M-AZU", "M", "Azul", "27.5", producto);
        };
    }

    @Test
    void crearYListarVariantesProducto() throws Exception {
        mockMvc.perform(post("/api/v1/variantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoVarianteProducto(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idVarianteProducto").exists())
                .andExpect(jsonPath("$.color").value("Rojo"));

        mockMvc.perform(post("/api/v1/variantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoVarianteProducto(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/variantes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.varianteProductoList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/variantes/sku/BIC-MTB29-L-NEG"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("BIC-MTB29-L-NEG"));
    }

    @Test
    void actualizarVarianteProducto() throws Exception {
        VarianteProducto guardado = varianteProductoRepository.save(nuevoVarianteProducto(1));

        mockMvc.perform(put("/api/v1/variantes/" + guardado.getIdVarianteProducto())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoVarianteProducto(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVarianteProducto").value(guardado.getIdVarianteProducto().intValue()))
                .andExpect(jsonPath("$.color").value("Azul"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.variantes.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        VarianteProducto guardado = varianteProductoRepository.save(nuevoVarianteProducto(1));

        mockMvc.perform(delete("/api/v1/variantes/" + guardado.getIdVarianteProducto()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/variantes/" + guardado.getIdVarianteProducto()))
                .andExpect(status().isNotFound());
    }
}
