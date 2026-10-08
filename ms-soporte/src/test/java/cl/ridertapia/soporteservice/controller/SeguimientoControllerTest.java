package cl.ridertapia.soporteservice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.soporteservice.dto.*;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.*;
import cl.ridertapia.soporteservice.service.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SeguimientoController.class)
@Import(SeguimientoModelAssembler.class)
class SeguimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeguimientoService seguimientoService;

    private SeguimientoDto.Respuesta seguimiento(Seguimiento.Estado estado) {
        return new SeguimientoDto.Respuesta(1L, 1L, "12345678-5", Seguimiento.Tipo.LLAMADA, estado, "Llamar",
                LocalDateTime.of(2026, 10, 7, 12, 0));
    }

    private static final String BODY = """
            {"idPedido":1,"rutUsuario":"12345678-5","tipo":"LLAMADA","observacion":"Llamar"}""";

    @Test
    @DisplayName("POST /seguimientos: 201 y enlace 'actualizarEstado' mientras está PENDIENTE")
    void registrar() throws Exception {
        when(seguimientoService.registrarSeguimiento(any())).thenReturn(seguimiento(Seguimiento.Estado.PENDIENTE));

        mockMvc.perform(post("/api/v1/seguimientos").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/seguimientos/1")))
                .andExpect(jsonPath("$._links.actualizarEstado").exists())
                .andExpect(jsonPath("$._links.pedido").exists());
    }

    @Test
    @DisplayName("POST /seguimientos: tipo inexistente responde 400")
    void registrar_tipoInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/seguimientos").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("LLAMADA", "PALOMA")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /seguimientos: faltan datos responde 400")
    void registrar_sinDatos() throws Exception {
        mockMvc.perform(post("/api/v1/seguimientos").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("idPedido")));
    }

    @Test
    @DisplayName("GET /seguimientos: lista HAL en _embedded.seguimientos")
    void listar() throws Exception {
        when(seguimientoService.listarSeguimientos()).thenReturn(List.of(seguimiento(Seguimiento.Estado.PENDIENTE)));

        mockMvc.perform(get("/api/v1/seguimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.seguimientos", hasSize(1)));
    }

    @Test
    @DisplayName("GET /seguimientos/{id}: uno realizado ya no ofrece 'actualizarEstado'")
    void obtener_realizado() throws Exception {
        when(seguimientoService.findById(1L)).thenReturn(seguimiento(Seguimiento.Estado.REALIZADO));

        mockMvc.perform(get("/api/v1/seguimientos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.actualizarEstado").doesNotExist());
    }

    @Test
    @DisplayName("GET /seguimientos/{id}: inexistente responde 404")
    void obtener_noExiste() throws Exception {
        when(seguimientoService.findById(9L)).thenThrow(ApiException.noEncontrado("No existe el seguimiento 9"));

        mockMvc.perform(get("/api/v1/seguimientos/9")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /seguimientos/pedido/{id}: seguimientos del pedido")
    void porPedido() throws Exception {
        when(seguimientoService.findByPedido(1L)).thenReturn(List.of(seguimiento(Seguimiento.Estado.PENDIENTE)));

        mockMvc.perform(get("/api/v1/seguimientos/pedido/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.seguimientos", hasSize(1)));
    }

    @Test
    @DisplayName("PUT /seguimientos/{id}/estado: actualiza el estado")
    void actualizar() throws Exception {
        when(seguimientoService.actualizarEstado(eq(1L), any())).thenReturn(seguimiento(Seguimiento.Estado.REALIZADO));

        mockMvc.perform(put("/api/v1/seguimientos/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"REALIZADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REALIZADO"));
    }

    @Test
    @DisplayName("PUT /seguimientos/{id}/estado: estado inexistente responde 400")
    void actualizar_invalido() throws Exception {
        mockMvc.perform(put("/api/v1/seguimientos/1/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"NUNCA\"}"))
                .andExpect(status().isBadRequest());
    }
}
