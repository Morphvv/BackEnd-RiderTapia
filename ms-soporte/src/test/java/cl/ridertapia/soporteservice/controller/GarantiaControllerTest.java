package cl.ridertapia.soporteservice.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.soporteservice.dto.*;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.*;
import cl.ridertapia.soporteservice.service.*;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GarantiaController.class)
@Import(GarantiaModelAssembler.class)
class GarantiaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GarantiaService garantiaService;

    private GarantiaDto.Respuesta garantia() {
        return new GarantiaDto.Respuesta(1L, "12345678-5", 1L, 100L, LocalDate.of(2026, 10, 7),
                LocalDate.of(2027, 10, 7), Garantia.Estado.VIGENTE);
    }

    @Test
    @DisplayName("POST /garantias: 201, Location y enlace a la vigencia")
    void crear() throws Exception {
        when(garantiaService.registrarGarantia(any())).thenReturn(garantia());

        mockMvc.perform(post("/api/v1/garantias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rutUsuario\":\"12345678-5\",\"idPedido\":1,\"idProducto\":100}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/garantias/1")))
                .andExpect(jsonPath("$.estado").value("VIGENTE"))
                .andExpect(jsonPath("$._links.vigencia.href", endsWith("/api/v1/garantias/1/vigencia")));
    }

    @Test
    @DisplayName("POST /garantias: meses de cobertura sobre 60 responde 400")
    void crear_invalida() throws Exception {
        mockMvc.perform(post("/api/v1/garantias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rutUsuario\":\"12345678-5\",\"idPedido\":1,\"idProducto\":100,\"mesesCobertura\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("mesesCobertura")));
    }

    @Test
    @DisplayName("POST /garantias: RUT mal escrito responde 400")
    void crear_rutInvalido() throws Exception {
        mockMvc.perform(post("/api/v1/garantias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rutUsuario\":\"abc\",\"idPedido\":1,\"idProducto\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("RUT")));
    }

    @Test
    @DisplayName("GET /garantias: lista HAL en _embedded.garantias")
    void listar() throws Exception {
        when(garantiaService.listarGarantias()).thenReturn(List.of(garantia()));

        mockMvc.perform(get("/api/v1/garantias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.garantias", hasSize(1)))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    @DisplayName("GET /garantias/{id}: inexistente responde 404")
    void obtener_noExiste() throws Exception {
        when(garantiaService.findById(9L)).thenThrow(ApiException.noEncontrado("No existe la garantía 9"));

        mockMvc.perform(get("/api/v1/garantias/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /garantias/usuario/{rut}: garantías del usuario")
    void porUsuario() throws Exception {
        when(garantiaService.findByRutUsuario("12345678-5")).thenReturn(List.of(garantia()));

        mockMvc.perform(get("/api/v1/garantias/usuario/12345678-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.garantias", hasSize(1)));
    }

    @Test
    @DisplayName("GET /garantias/{id}/vigencia: días restantes con enlaces")
    void vigencia() throws Exception {
        when(garantiaService.verificarVigencia(1L))
                .thenReturn(new GarantiaDto.Vigencia(1L, true, LocalDate.of(2027, 10, 7), 365));

        mockMvc.perform(get("/api/v1/garantias/1/vigencia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vigente").value(true))
                .andExpect(jsonPath("$.diasRestantes").value(365))
                .andExpect(jsonPath("$._links.garantia").exists());
    }
}
