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
class DevolucionControllerIT {

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
    private DevolucionRepository devolucionRepository;

    @BeforeEach
    void limpiar() {
        devolucionRepository.deleteAll();
    }

    private static final String BODY =
            "{\"rutUsuario\":\"12345678-5\",\"idPedido\":1,\"idProducto\":100,\"motivo\":\"Llegó con la llanta dañada\"}";

    private Long solicitar() throws Exception {
        mockMvc.perform(post("/api/v1/devoluciones").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        return devolucionRepository.findAll().get(0).getIdDevolucion();
    }

    private void resolver(Long id, String json, int esperado) throws Exception {
        mockMvc.perform(put("/api/v1/devoluciones/" + id + "/resolver").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().is(esperado));
    }

    @Test
    void testFlujoCompletoAprobarYCompletar() throws Exception {
        Long id = solicitar();

        resolver(id, "{\"estado\":\"APROBADA\",\"respuesta\":\"Envíalo a la tienda\"}", 200);
        resolver(id, "{\"estado\":\"COMPLETADA\"}", 200);

        Devolucion enBd = devolucionRepository.findById(id).orElseThrow();
        assertThat(enBd.getEstado()).isEqualTo(Devolucion.Estado.COMPLETADA);
        assertThat(enBd.getFechaResolucion()).isNotNull();
        assertThat(enBd.getRespuesta()).isEqualTo("Envíalo a la tienda");

        mockMvc.perform(get("/api/v1/devoluciones/" + id))
                .andExpect(jsonPath("$._links.resolver").doesNotExist());
    }

    @Test
    void testRechazoExigeMotivo() throws Exception {
        Long id = solicitar();

        resolver(id, "{\"estado\":\"RECHAZADA\"}", 400);
        resolver(id, "{\"estado\":\"RECHAZADA\",\"respuesta\":\"Fuera de plazo\"}", 200);

        assertThat(devolucionRepository.findById(id).orElseThrow().getEstado()).isEqualTo(Devolucion.Estado.RECHAZADA);
    }

    @Test
    void testTransicionInvalidaDa409() throws Exception {
        Long id = solicitar();

        resolver(id, "{\"estado\":\"COMPLETADA\"}", 409);

        assertThat(devolucionRepository.findById(id).orElseThrow().getEstado()).isEqualTo(Devolucion.Estado.SOLICITADA);
    }

    @Test
    void testNoSePuedeSolicitarOtraEnCurso() throws Exception {
        solicitar();
        mockMvc.perform(post("/api/v1/devoluciones").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
        assertThat(devolucionRepository.count()).isEqualTo(1);
    }

    @Test
    void testDespuesDeRechazadaSePuedeSolicitarDeNuevo() throws Exception {
        Long id = solicitar();
        resolver(id, "{\"estado\":\"RECHAZADA\",\"respuesta\":\"Fuera de plazo\"}", 200);

        mockMvc.perform(post("/api/v1/devoluciones").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        assertThat(devolucionRepository.count()).isEqualTo(2);
    }

    @Test
    void testFiltrarPorEstadoYUsuario() throws Exception {
        solicitar();

        mockMvc.perform(get("/api/v1/devoluciones").param("estado", "SOLICITADA"))
                .andExpect(jsonPath("$._embedded.devoluciones", hasSize(1)));
        mockMvc.perform(get("/api/v1/devoluciones").param("estado", "APROBADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
        mockMvc.perform(get("/api/v1/devoluciones/usuario/12345678-5"))
                .andExpect(jsonPath("$._embedded.devoluciones", hasSize(1)));
    }
}
