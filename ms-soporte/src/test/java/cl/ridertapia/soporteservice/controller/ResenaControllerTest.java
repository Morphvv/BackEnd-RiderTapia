package cl.ridertapia.soporteservice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@WebMvcTest(ResenaController.class)
@Import(ResenaModelAssembler.class)
class ResenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResenaService resenaService;

    private ResenaDto.Respuesta resena(Resena.Estado estado) {
        return new ResenaDto.Respuesta(1L, "12345678-5", 100L, 5, "Excelente", LocalDateTime.of(2026, 10, 7, 12, 0), estado);
    }

    private static final String BODY = """
            {"rutUsuario":"12345678-5","idProducto":100,"calificacion":5,"comentario":"Excelente"}""";

    @Test
    @DisplayName("POST /resenas: 201, Location y enlaces")
    void crear() throws Exception {
        when(resenaService.guardarResena(any())).thenReturn(resena(Resena.Estado.VISIBLE));

        mockMvc.perform(post("/api/v1/soporte/resenas").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/soporte/resenas/1")))
                .andExpect(jsonPath("$.idResena").value(1))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/soporte/resenas/1")))
                .andExpect(jsonPath("$._links.ocultar").exists());
    }

    @Test
    @DisplayName("POST /resenas: calificación fuera de rango responde 400")
    void crear_invalida() throws Exception {
        String body = BODY.replace("\"calificacion\":5", "\"calificacion\":9");

        mockMvc.perform(post("/api/v1/soporte/resenas").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("calificacion")));
    }

    @Test
    @DisplayName("POST /resenas: reseña repetida responde 409 con el cuerpo de error")
    void crear_duplicada() throws Exception {
        when(resenaService.guardarResena(any())).thenThrow(ApiException.conflicto("Ya reseñaste este producto"));

        mockMvc.perform(post("/api/v1/soporte/resenas").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("Ya reseñaste este producto"));
    }

    @Test
    @DisplayName("GET /resenas: lista HAL en _embedded.resenas")
    void listar() throws Exception {
        when(resenaService.listarResenas()).thenReturn(List.of(resena(Resena.Estado.VISIBLE)));

        mockMvc.perform(get("/api/v1/soporte/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.resenas", hasSize(1)))
                .andExpect(jsonPath("$._embedded.resenas[0]._links.self.href", endsWith("/api/v1/soporte/resenas/1")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/soporte/resenas")));
    }

    @Test
    @DisplayName("GET /resenas/{id}: una reseña oculta no ofrece el enlace 'ocultar'")
    void obtener_oculta() throws Exception {
        when(resenaService.findById(1L)).thenReturn(resena(Resena.Estado.OCULTA));

        mockMvc.perform(get("/api/v1/soporte/resenas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OCULTA"))
                .andExpect(jsonPath("$._links.ocultar").doesNotExist())
                .andExpect(jsonPath("$._links.producto").exists());
    }

    @Test
    @DisplayName("GET /resenas/{id}: inexistente responde 404")
    void obtener_noExiste() throws Exception {
        when(resenaService.findById(9L)).thenThrow(ApiException.noEncontrado("No existe la reseña 9"));

        mockMvc.perform(get("/api/v1/soporte/resenas/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe la reseña 9"));
    }

    @Test
    @DisplayName("GET /resenas/{id}: un id que no es número responde 400")
    void obtener_idInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/soporte/resenas/abc")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /resenas/producto/{id}: lista vacía no trae _embedded pero sí enlaces")
    void porProducto_vacia() throws Exception {
        when(resenaService.findByProducto(100L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/soporte/resenas/producto/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.resumen").exists());
    }

    @Test
    @DisplayName("GET /resenas/producto/{id}/resumen: promedio con enlaces")
    void resumen() throws Exception {
        when(resenaService.resumenProducto(100L)).thenReturn(new ResenaDto.Resumen(100L, 4.5, 2));

        mockMvc.perform(get("/api/v1/soporte/resenas/producto/100/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promedio").value(4.5))
                .andExpect(jsonPath("$.totalResenas").value(2))
                .andExpect(jsonPath("$._links.resenas").exists());
    }

    @Test
    @DisplayName("GET /resenas/usuario/{rut}: reseñas del usuario")
    void porUsuario() throws Exception {
        when(resenaService.findByRutUsuario("12345678-5")).thenReturn(List.of(resena(Resena.Estado.VISIBLE)));

        mockMvc.perform(get("/api/v1/soporte/resenas/usuario/12345678-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.resenas", hasSize(1)));
    }

    @Test
    @DisplayName("PATCH /resenas/{id}/ocultar: devuelve la reseña oculta")
    void ocultar() throws Exception {
        when(resenaService.ocultarResena(1L)).thenReturn(resena(Resena.Estado.OCULTA));

        mockMvc.perform(patch("/api/v1/soporte/resenas/1/ocultar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OCULTA"));
    }
}
