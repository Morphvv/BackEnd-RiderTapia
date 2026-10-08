package cl.ridertapia.soporteservice.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class GarantiaControllerIT {

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
    private GarantiaRepository garantiaRepository;

    @BeforeEach
    void limpiar() {
        garantiaRepository.deleteAll();
    }

    private static final String BODY = "{\"rutUsuario\":\"12345678-5\",\"idPedido\":1,\"idProducto\":100}";

    @Test
    void testRegistrarYObtenerGarantia() throws Exception {
        mockMvc.perform(post("/api/v1/garantias").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("VIGENTE"))
                .andExpect(jsonPath("$._links.vigencia").exists());

        Long id = garantiaRepository.findAll().get(0).getIdGarantia();
        mockMvc.perform(get("/api/v1/garantias/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value(100));
        mockMvc.perform(get("/api/v1/garantias/usuario/12345678-5"))
                .andExpect(jsonPath("$._embedded.garantias", hasSize(1)));
        mockMvc.perform(get("/api/v1/garantias/" + id + "/vigencia"))
                .andExpect(jsonPath("$.vigente").value(true))
                .andExpect(jsonPath("$.diasRestantes").value(org.hamcrest.Matchers.greaterThan(300)));
    }

    @Test
    void testGarantiaDuplicadaDa409() throws Exception {
        mockMvc.perform(post("/api/v1/garantias").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/garantias").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isConflict());
        assertThat(garantiaRepository.count()).isEqualTo(1);
    }

    @Test
    void testGarantiaVencidaSeMarcaExpirada() throws Exception {
        Long id = garantiaRepository.save(Garantia.builder().rutUsuario("12345678-5").idPedido(2L).idProducto(100L)
                .fechaInicio(java.time.LocalDate.now().minusMonths(13)).fechaTermino(java.time.LocalDate.now().minusMonths(1))
                .estado(Garantia.Estado.VIGENTE).build()).getIdGarantia();

        mockMvc.perform(get("/api/v1/garantias/" + id + "/vigencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigente").value(false))
                .andExpect(jsonPath("$.diasRestantes").value(0));

        assertThat(garantiaRepository.findById(id).orElseThrow().getEstado()).isEqualTo(Garantia.Estado.EXPIRADA);
    }

    @Test
    void testGarantiaInexistenteDa404() throws Exception {
        mockMvc.perform(get("/api/v1/garantias/999")).andExpect(status().isNotFound());
    }
}
