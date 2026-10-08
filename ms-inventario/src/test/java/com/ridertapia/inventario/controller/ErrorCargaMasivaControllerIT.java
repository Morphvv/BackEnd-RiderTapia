package com.ridertapia.inventario.controller;

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

import com.ridertapia.inventario.LimpiezaBaseDatos;
import com.ridertapia.inventario.model.CargaMasiva;
import com.ridertapia.inventario.model.ErrorCargaMasiva;

import com.ridertapia.inventario.repository.CargaMasivaRepository;
import com.ridertapia.inventario.repository.ErrorCargaMasivaRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ErrorCargaMasivaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private ErrorCargaMasivaRepository errorCargaMasivaRepository;

    @Autowired
    private CargaMasivaRepository cargaMasivaRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    private CargaMasiva cargaMasiva;

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
        cargaMasiva = cargaMasivaRepository.save(new CargaMasiva(null, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L));
    }

    private ErrorCargaMasiva nuevoErrorCargaMasiva(int variante) {
        return switch (variante) {
            case 1 -> new ErrorCargaMasiva(null, 15, "SKU repetido", cargaMasiva);
            case 2 -> new ErrorCargaMasiva(null, 87, "Precio vacío", cargaMasiva);
            default -> new ErrorCargaMasiva(null, 16, "SKU no existe", cargaMasiva);
        };
    }

    @Test
    void crearYListarErroresCargaMasiva() throws Exception {
        mockMvc.perform(post("/api/v1/errores-carga")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoErrorCargaMasiva(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idErrorCargaMasiva").exists())
                .andExpect(jsonPath("$.mensaje").value("SKU repetido"));

        mockMvc.perform(post("/api/v1/errores-carga")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoErrorCargaMasiva(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/errores-carga"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.errorCargaMasivaList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/errores-carga/cargaMasiva/" + cargaMasiva.getIdCargaMasiva()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.errorCargaMasivaList", hasSize(2)));
    }

    @Test
    void actualizarErrorCargaMasiva() throws Exception {
        ErrorCargaMasiva guardado = errorCargaMasivaRepository.save(nuevoErrorCargaMasiva(1));

        mockMvc.perform(put("/api/v1/errores-carga/" + guardado.getIdErrorCargaMasiva())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoErrorCargaMasiva(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idErrorCargaMasiva").value(guardado.getIdErrorCargaMasiva().intValue()))
                .andExpect(jsonPath("$.mensaje").value("SKU no existe"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.erroresCarga.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        ErrorCargaMasiva guardado = errorCargaMasivaRepository.save(nuevoErrorCargaMasiva(1));

        mockMvc.perform(delete("/api/v1/errores-carga/" + guardado.getIdErrorCargaMasiva()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/errores-carga/" + guardado.getIdErrorCargaMasiva()))
                .andExpect(status().isNotFound());
    }
}
