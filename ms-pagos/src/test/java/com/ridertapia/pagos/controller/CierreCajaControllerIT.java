package com.ridertapia.pagos.controller;

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

import com.ridertapia.pagos.LimpiezaBaseDatos;
import com.ridertapia.pagos.model.CierreCaja;

import com.ridertapia.pagos.repository.CierreCajaRepository;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CierreCajaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LimpiezaBaseDatos limpiezaBaseDatos;

    @Autowired
    private CierreCajaRepository cierreCajaRepository;

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @BeforeEach
    void prepararBase() {
        limpiezaBaseDatos.limpiar();
    }

    private CierreCaja nuevoCierreCaja(int variante) {
        return switch (variante) {
            case 1 -> new CierreCaja(null, LocalDate.of(2026, 10, 6), 250000, 420000, 899990, 79990, 250000, 0, 1L);
            case 2 -> new CierreCaja(null, LocalDate.of(2026, 10, 7), 180000, 310000, 0, 120000, 175000, -5000, 2L);
            default -> new CierreCaja(null, LocalDate.of(2026, 10, 8), 250000, 420000, 899990, 79990, 249000, -1000, 1L);
        };
    }

    @Test
    void crearYListarCierresCaja() throws Exception {
        mockMvc.perform(post("/api/v1/cierres-caja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCierreCaja(1))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCierreCaja").exists())
                .andExpect(jsonPath("$.efectivoContado").value(250000));

        mockMvc.perform(post("/api/v1/cierres-caja")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCierreCaja(2))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/cierres-caja"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cierreCajaList", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href").exists());

        mockMvc.perform(get("/api/v1/cierres-caja/idEmpleado/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.cierreCajaList", hasSize(1)));
    }

    @Test
    void actualizarCierreCaja() throws Exception {
        CierreCaja guardado = cierreCajaRepository.save(nuevoCierreCaja(1));

        mockMvc.perform(put("/api/v1/cierres-caja/" + guardado.getIdCierreCaja())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoCierreCaja(3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCierreCaja").value(guardado.getIdCierreCaja().intValue()))
                .andExpect(jsonPath("$.efectivoContado").value(249000))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.cierresCaja.href").exists());
    }

    @Test
    void eliminarYVerificar404() throws Exception {
        CierreCaja guardado = cierreCajaRepository.save(nuevoCierreCaja(1));

        mockMvc.perform(delete("/api/v1/cierres-caja/" + guardado.getIdCierreCaja()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/cierres-caja/" + guardado.getIdCierreCaja()))
                .andExpect(status().isNotFound());
    }
}
