package cl.ridertapia.soporteservice.controller;

import static org.hamcrest.Matchers.containsString;
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
class SeguimientoControllerIT {

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
    private SeguimientoRepository seguimientoRepository;

    @BeforeEach
    void limpiar() {
        seguimientoRepository.deleteAll();
    }

    private static final String BODY = "{\"idPedido\":1,\"rutUsuario\":\"12345678-5\",\"tipo\":\"ENCUESTA\",\"observacion\":\"Preguntar por la entrega\"}";

    @Test
    void testRegistrarYConsultarPorPedido() throws Exception {
        mockMvc.perform(post("/api/v1/seguimientos").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$._links.actualizarEstado").exists());

        mockMvc.perform(get("/api/v1/seguimientos/pedido/1"))
                .andExpect(jsonPath("$._embedded.seguimientos", hasSize(1)))
                .andExpect(jsonPath("$._embedded.seguimientos[0].tipo").value("ENCUESTA"));
        mockMvc.perform(get("/api/v1/seguimientos/pedido/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void testActualizarEstado() throws Exception {
        mockMvc.perform(post("/api/v1/seguimientos").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        Long id = seguimientoRepository.findAll().get(0).getIdSeguimiento();

        mockMvc.perform(put("/api/v1/seguimientos/" + id + "/estado").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"REALIZADO\",\"observacion\":\"Cliente conforme\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REALIZADO"))
                .andExpect(jsonPath("$.observacion").value("Cliente conforme"))
                .andExpect(jsonPath("$._links.actualizarEstado").doesNotExist());

        mockMvc.perform(get("/api/v1/seguimientos/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REALIZADO"));
    }

    @Test
    void testSeguimientoInexistenteDa404() throws Exception {
        mockMvc.perform(get("/api/v1/seguimientos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("999")));
    }

    @Test
    void testRegistrarSinCuentaDa404() throws Exception {
        when(usuarioClient.obtenerPorRut("12345678-5")).thenThrow(ApiException.noEncontrado("sin cuenta"));

        mockMvc.perform(post("/api/v1/seguimientos").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNotFound());
        assertThat(seguimientoRepository.count()).isZero();
    }
}
