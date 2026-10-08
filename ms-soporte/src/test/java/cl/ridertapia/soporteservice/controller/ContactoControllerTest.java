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

@WebMvcTest(ContactoController.class)
@Import(ContactoModelAssembler.class)
class ContactoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactoService contactoService;

    private ContactoDto.Respuesta contacto(Contacto.Estado estado) {
        return new ContactoDto.Respuesta(1L, null, "Juan", "juan@example.com", "Duda", "Hola", estado,
                null, LocalDateTime.of(2026, 10, 7, 12, 0));
    }

    private static final String BODY = """
            {"nombre":"Juan","emailContacto":"juan@example.com","asunto":"Duda","mensaje":"Hola"}""";

    @Test
    @DisplayName("POST /contactos: un visitante sin RUT puede escribir")
    void enviar_sinRut() throws Exception {
        when(contactoService.enviarContacto(any())).thenReturn(contacto(Contacto.Estado.ABIERTO));

        mockMvc.perform(post("/api/v1/contactos").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/contactos/1")))
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andExpect(jsonPath("$._links.responder").exists());
    }

    @Test
    @DisplayName("POST /contactos: email inválido responde 400")
    void enviar_emailInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/contactos").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("juan@example.com", "no-es-email")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("emailContacto")));
    }

    @Test
    @DisplayName("POST /contactos: RUT enviado pero mal escrito responde 400")
    void enviar_rutInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/contactos").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("{", "{\"rutUsuario\":\"123\",")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("RUT")));
    }

    @Test
    @DisplayName("POST /contactos: cuerpo con JSON roto responde 400")
    void enviar_jsonRoto() throws Exception {
        mockMvc.perform(post("/api/v1/contactos").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /contactos?estado=: lista HAL en _embedded.contactos")
    void listar() throws Exception {
        when(contactoService.listarContactos(Contacto.Estado.ABIERTO)).thenReturn(List.of(contacto(Contacto.Estado.ABIERTO)));

        mockMvc.perform(get("/api/v1/contactos").param("estado", "ABIERTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.contactos", hasSize(1)));
    }

    @Test
    @DisplayName("GET /contactos/{id}: un mensaje respondido no ofrece 'responder'")
    void obtener_respondido() throws Exception {
        when(contactoService.findById(1L)).thenReturn(contacto(Contacto.Estado.RESPONDIDO));

        mockMvc.perform(get("/api/v1/contactos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.responder").doesNotExist());
    }

    @Test
    @DisplayName("GET /contactos/{id}: inexistente responde 404")
    void obtener_noExiste() throws Exception {
        when(contactoService.findById(9L)).thenThrow(ApiException.noEncontrado("No existe el mensaje de contacto 9"));

        mockMvc.perform(get("/api/v1/contactos/9")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /contactos/{id}/responder: responde el mensaje")
    void responder() throws Exception {
        when(contactoService.responderContacto(eq(1L), any())).thenReturn(contacto(Contacto.Estado.RESPONDIDO));

        mockMvc.perform(put("/api/v1/contactos/1/responder").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respuesta\":\"Claro que sí\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESPONDIDO"));
    }

    @Test
    @DisplayName("PUT /contactos/{id}/responder: respuesta vacía responde 400")
    void responder_vacia() throws Exception {
        mockMvc.perform(put("/api/v1/contactos/1/responder").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respuesta\":\" \"}"))
                .andExpect(status().isBadRequest());
    }
}
