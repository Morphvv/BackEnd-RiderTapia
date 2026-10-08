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

@WebMvcTest(DevolucionController.class)
@Import(DevolucionModelAssembler.class)
class DevolucionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DevolucionService devolucionService;

    private DevolucionDto.Respuesta devolucion(Devolucion.Estado estado) {
        return new DevolucionDto.Respuesta(1L, "12345678-5", 1L, 100L, "Llegó con la llanta dañada", estado,
                LocalDateTime.of(2026, 10, 7, 12, 0), null, null);
    }

    private static final String BODY = """
            {"rutUsuario":"12345678-5","idPedido":1,"idProducto":100,"motivo":"Llegó con la llanta dañada"}""";

    @Test
    @DisplayName("POST /devoluciones: 201 y enlace 'resolver' mientras está SOLICITADA")
    void solicitar() throws Exception {
        when(devolucionService.solicitarDevolucion(any())).thenReturn(devolucion(Devolucion.Estado.SOLICITADA));

        mockMvc.perform(post("/api/v1/devoluciones").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/devoluciones/1")))
                .andExpect(jsonPath("$.estado").value("SOLICITADA"))
                .andExpect(jsonPath("$._links.resolver").exists());
    }

    @Test
    @DisplayName("POST /devoluciones: motivo muy corto responde 400")
    void solicitar_motivoCorto() throws Exception {
        mockMvc.perform(post("/api/v1/devoluciones").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("Llegó con la llanta dañada", "malo")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("motivo")));
    }

    @Test
    @DisplayName("POST /devoluciones: otra en curso responde 409")
    void solicitar_enCurso() throws Exception {
        when(devolucionService.solicitarDevolucion(any())).thenThrow(ApiException.conflicto("Ya hay una devolución en curso"));

        mockMvc.perform(post("/api/v1/devoluciones").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /devoluciones?estado=: filtra por estado")
    void listar_conEstado() throws Exception {
        when(devolucionService.listarDevoluciones(Devolucion.Estado.APROBADA))
                .thenReturn(List.of(devolucion(Devolucion.Estado.APROBADA)));

        mockMvc.perform(get("/api/v1/devoluciones").param("estado", "APROBADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.devoluciones", hasSize(1)))
                .andExpect(jsonPath("$._embedded.devoluciones[0]._links.resolver").exists());
    }

    @Test
    @DisplayName("GET /devoluciones?estado=XXX: estado inexistente responde 400")
    void listar_estadoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/devoluciones").param("estado", "XXX"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("estado")));
    }

    @Test
    @DisplayName("GET /devoluciones/{id}: una devolución completada no ofrece 'resolver'")
    void obtener_completada() throws Exception {
        when(devolucionService.findById(1L)).thenReturn(devolucion(Devolucion.Estado.COMPLETADA));

        mockMvc.perform(get("/api/v1/devoluciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.resolver").doesNotExist())
                .andExpect(jsonPath("$._links.usuario").exists());
    }

    @Test
    @DisplayName("GET /devoluciones/usuario/{rut}: devoluciones del usuario")
    void porUsuario() throws Exception {
        when(devolucionService.findByRutUsuario("12345678-5")).thenReturn(List.of(devolucion(Devolucion.Estado.SOLICITADA)));

        mockMvc.perform(get("/api/v1/devoluciones/usuario/12345678-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.devoluciones", hasSize(1)));
    }

    @Test
    @DisplayName("PUT /devoluciones/{id}/resolver: aprueba")
    void resolver() throws Exception {
        when(devolucionService.resolverDevolucion(eq(1L), any())).thenReturn(devolucion(Devolucion.Estado.APROBADA));

        mockMvc.perform(put("/api/v1/devoluciones/1/resolver").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"APROBADA\",\"respuesta\":\"Envíalo a la tienda\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
    }

    @Test
    @DisplayName("PUT /devoluciones/{id}/resolver: transición no permitida responde 409")
    void resolver_transicionInvalida() throws Exception {
        when(devolucionService.resolverDevolucion(eq(1L), any()))
                .thenThrow(ApiException.conflicto("No se puede pasar de COMPLETADA a APROBADA"));

        mockMvc.perform(put("/api/v1/devoluciones/1/resolver").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"APROBADA\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /devoluciones/{id}/resolver: sin estado o con estado inventado responde 400")
    void resolver_invalido() throws Exception {
        mockMvc.perform(put("/api/v1/devoluciones/1/resolver").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/v1/devoluciones/1/resolver").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"INVENTADO\"}"))
                .andExpect(status().isBadRequest());
    }
}
