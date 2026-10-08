package cl.ridertapia.soporteservice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class ResenaControllerIT {

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
    private ResenaRepository resenaRepository;

    @BeforeEach
    void limpiar() {
        resenaRepository.deleteAll();
    }

    private static final String BODY = "{\"rutUsuario\":\"12345678-5\",\"idProducto\":100,\"calificacion\":%d,\"comentario\":\"Buena\"}";

    @Test
    void testCrearYListarResenas() throws Exception {
        mockMvc.perform(post("/api/v1/soporte/resenas").contentType(MediaType.APPLICATION_JSON).content(String.format(BODY, 5)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("VISIBLE"))
                .andExpect(jsonPath("$._links.self").exists());

        mockMvc.perform(get("/api/v1/soporte/resenas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.resenas", hasSize(1)))
                .andExpect(jsonPath("$._embedded.resenas[0].calificacion").value(5));
        assertThat(resenaRepository.count()).isEqualTo(1);
    }

    @Test
    void testResenaDuplicadaDa409() throws Exception {
        mockMvc.perform(post("/api/v1/soporte/resenas").contentType(MediaType.APPLICATION_JSON).content(String.format(BODY, 5)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/soporte/resenas").contentType(MediaType.APPLICATION_JSON).content(String.format(BODY, 3)))
                .andExpect(status().isConflict());
        assertThat(resenaRepository.count()).isEqualTo(1);
    }

    @Test
    void testUsuarioSinCuentaDa404() throws Exception {
        when(usuarioClient.obtenerPorRut("12345678-5")).thenThrow(ApiException.noEncontrado("No existe una cuenta"));

        mockMvc.perform(post("/api/v1/soporte/resenas").contentType(MediaType.APPLICATION_JSON).content(String.format(BODY, 5)))
                .andExpect(status().isNotFound());
        assertThat(resenaRepository.count()).isZero();
    }

    @Test
    void testOcultarResenaYResumen() throws Exception {
        resenaRepository.save(Resena.builder().rutUsuario("12345678-5").idProducto(100L).calificacion(5)
                .fecha(java.time.LocalDateTime.now()).estado(Resena.Estado.VISIBLE).build());
        Long oculta = resenaRepository.save(Resena.builder().rutUsuario("11111111-1").idProducto(100L).calificacion(1)
                .fecha(java.time.LocalDateTime.now()).estado(Resena.Estado.VISIBLE).build()).getIdResena();

        mockMvc.perform(get("/api/v1/soporte/resenas/producto/100/resumen"))
                .andExpect(jsonPath("$.promedio").value(3.0))
                .andExpect(jsonPath("$.totalResenas").value(2));

        mockMvc.perform(patch("/api/v1/soporte/resenas/" + oculta + "/ocultar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("OCULTA"))
                .andExpect(jsonPath("$._links.ocultar").doesNotExist());

        mockMvc.perform(get("/api/v1/soporte/resenas/producto/100"))
                .andExpect(jsonPath("$._embedded.resenas", hasSize(1)));
        mockMvc.perform(get("/api/v1/soporte/resenas/producto/100/resumen"))
                .andExpect(jsonPath("$.promedio").value(5.0))
                .andExpect(jsonPath("$.totalResenas").value(1));
    }

    @Test
    void testResenaInexistenteDa404() throws Exception {
        mockMvc.perform(get("/api/v1/soporte/resenas/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje", containsString("999")));
    }
}
