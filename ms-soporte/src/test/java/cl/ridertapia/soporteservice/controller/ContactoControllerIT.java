package cl.ridertapia.soporteservice.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.client.UsuarioDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.*;
import cl.ridertapia.soporteservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class ContactoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioClient usuarioClient;

    @BeforeEach
    void usuarioExiste() {
        when(usuarioClient.obtenerPorRut(anyString())).thenAnswer(i ->
                new UsuarioDto(i.getArgument(0), "juan@example.com", "CLIENTE"));
    }

    @Autowired
    private ContactoRepository contactoRepository;

    @BeforeEach
    void limpiar() {
        contactoRepository.deleteAll();
    }

    private static final String BODY = "{\"nombre\":\"Juan\",\"emailContacto\":\"JUAN@Example.com\",\"asunto\":\"Duda\",\"mensaje\":\"Hola\"}";

    @Test
    void testVisitanteSinRutPuedeEscribir() throws Exception {
        mockMvc.perform(post("/api/v1/contactos").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTO"))
                .andExpect(jsonPath("$.emailContacto").value("juan@example.com"))
                .andExpect(jsonPath("$.rutUsuario").doesNotExist());

        Contacto enBd = contactoRepository.findAll().get(0);
        assertThat(enBd.getRutUsuario()).isNull();
    }

    @Test
    void testContactoConRutSinCuentaDa404() throws Exception {
        when(usuarioClient.obtenerPorRut("12345678-5")).thenThrow(ApiException.noEncontrado("sin cuenta"));

        mockMvc.perform(post("/api/v1/contactos").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.replace("{", "{\"rutUsuario\":\"12345678-5\",")))
                .andExpect(status().isNotFound());
        assertThat(contactoRepository.count()).isZero();
    }

    @Test
    void testResponderYFiltrarPorEstado() throws Exception {
        mockMvc.perform(post("/api/v1/contactos").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        Long id = contactoRepository.findAll().get(0).getIdContacto();

        mockMvc.perform(get("/api/v1/contactos").param("estado", "ABIERTO"))
                .andExpect(jsonPath("$._embedded.contactos", hasSize(1)));

        mockMvc.perform(put("/api/v1/contactos/" + id + "/responder").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respuesta\":\"Claro que sí\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESPONDIDO"))
                .andExpect(jsonPath("$.respuesta").value("Claro que sí"))
                .andExpect(jsonPath("$._links.responder").doesNotExist());

        mockMvc.perform(get("/api/v1/contactos").param("estado", "ABIERTO"))
                .andExpect(jsonPath("$._embedded").doesNotExist());
        mockMvc.perform(get("/api/v1/contactos").param("estado", "RESPONDIDO"))
                .andExpect(jsonPath("$._embedded.contactos", hasSize(1)));
    }

    @Test
    void testContactoInexistenteDa404() throws Exception {
        mockMvc.perform(get("/api/v1/contactos/999")).andExpect(status().isNotFound());
        mockMvc.perform(put("/api/v1/contactos/999/responder").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respuesta\":\"x\"}"))
                .andExpect(status().isNotFound());
    }
}
