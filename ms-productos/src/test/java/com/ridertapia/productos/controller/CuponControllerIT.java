package com.ridertapia.productos.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import com.ridertapia.productos.model.Cupon;
import com.ridertapia.productos.model.TipoDescuento;
import com.ridertapia.productos.repository.CuponRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CuponControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private CuponRepository cuponRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();


    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();

    }

    private Cupon nuevoCupon(int variante) {
        return switch (variante) {
            case 1 -> new Cupon(null, "RIDER10", TipoDescuento.PORCENTAJE, 10, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 100, 0, true, new ArrayList<>());
            case 2 -> new Cupon(null, "BIENVENIDA5000", TipoDescuento.MONTO_FIJO, 5000, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 50, 0, true, new ArrayList<>());
            default -> new Cupon(null, "RIDER15", TipoDescuento.PORCENTAJE, 15, LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 30), 200, 10, false, new ArrayList<>());
        };
    }

    @Test
    void crearYListarCupones() throws Exception {
        mockMvc.perform(post("/api/v1/cupones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCupon(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCupon").exists())
                .andExpect(jsonPath("$.valor").value(10));

        mockMvc.perform(post("/api/v1/cupones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCupon(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/cupones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cuponList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/cupones/codigo/BIENVENIDA5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("BIENVENIDA5000"));
    }

    @Test
    void actualizarCupon() throws Exception {
        Cupon guardado = cuponRepository.save(nuevoCupon(1));

        mockMvc.perform(put("/api/v1/cupones/" + guardado.getIdCupon())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCupon(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCupon").value(guardado.getIdCupon().intValue()))
                .andExpect(jsonPath("$.valor").value(15))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cupones.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        Cupon guardado = cuponRepository.save(nuevoCupon(1));

        mockMvc.perform(delete("/api/v1/cupones/" + guardado.getIdCupon()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cupones/" + guardado.getIdCupon()))
                .andExpect(status().isNotFound());
    }
}
