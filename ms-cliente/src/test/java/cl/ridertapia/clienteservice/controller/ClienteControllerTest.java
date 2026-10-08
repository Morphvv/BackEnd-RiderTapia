package cl.ridertapia.clienteservice.controller;

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

import cl.ridertapia.clienteservice.dto.ClienteRequest;
import cl.ridertapia.clienteservice.dto.ClienteResponse;
import cl.ridertapia.clienteservice.dto.ClienteUpdateRequest;
import cl.ridertapia.clienteservice.exception.ApiException;
import cl.ridertapia.clienteservice.service.ClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
@Import(ClienteModelAssembler.class)
class ClienteControllerTest {

    private static final String RUT = "12345678-5";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClienteService clienteService;

    private ClienteResponse respuesta() {
        return new ClienteResponse(RUT, "Matias", "Dominguez", "+56912345678", "Calle Falsa 123", "Concepción");
    }

    private ClienteRequest request() {
        return new ClienteRequest(RUT, "Matias", "Dominguez", "+56912345678", "Calle Falsa 123", "Concepción");
    }

    @Test
    @DisplayName("POST /api/v1/clientes → 201, Location y enlace self")
    void guardarCliente_ok() throws Exception {
        when(clienteService.guardarCliente(any(ClienteRequest.class))).thenReturn(respuesta());

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/clientes/" + RUT)))
                .andExpect(jsonPath("$.rutUsuario").value(RUT))
                .andExpect(jsonPath("$.nombre").value("Matias"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/clientes/" + RUT)))
                .andExpect(jsonPath("$._links.clientes.href", endsWith("/api/v1/clientes")));
    }

    @Test
    @DisplayName("POST /api/v1/clientes con datos inválidos → 400 con el detalle de cada campo")
    void guardarCliente_validacion() throws Exception {
        ClienteRequest invalido = new ClienteRequest("123", "", " ", "abc", null, null);

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensaje", containsString("rutUsuario")))
                .andExpect(jsonPath("$.mensaje", containsString("nombre")))
                .andExpect(jsonPath("$.mensaje", containsString("apellido")))
                .andExpect(jsonPath("$.mensaje", containsString("telefono")));
    }

    @Test
    @DisplayName("POST /api/v1/clientes con perfil existente → 409")
    void guardarCliente_conflicto() throws Exception {
        when(clienteService.guardarCliente(any(ClienteRequest.class)))
                .thenThrow(ApiException.conflicto("Ya existe un perfil de cliente con el RUT " + RUT));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString(RUT)));
    }

    @Test
    @DisplayName("POST /api/v1/clientes sin cuenta de usuario → 404")
    void guardarCliente_sinCuenta() throws Exception {
        when(clienteService.guardarCliente(any(ClienteRequest.class)))
                .thenThrow(ApiException.noEncontrado("No existe una cuenta de usuario con el RUT " + RUT));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/clientes con usuario-service caído → 503")
    void guardarCliente_servicioNoDisponible() throws Exception {
        when(clienteService.guardarCliente(any(ClienteRequest.class)))
                .thenThrow(ApiException.servicioNoDisponible("usuario-service no disponible"));

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request())))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    @DisplayName("GET /api/v1/clientes → colección HAL con enlaces por cliente")
    void listarClientes() throws Exception {
        when(clienteService.listarClientes()).thenReturn(List.of(
                respuesta(), new ClienteResponse("11111111-1", "Ana", "Pérez", null, null, null)));

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.clientes", hasSize(2)))
                .andExpect(jsonPath("$._embedded.clientes[0].nombre").value("Matias"))
                .andExpect(jsonPath("$._embedded.clientes[0]._links.self.href", endsWith("/api/v1/clientes/" + RUT)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/clientes")));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{rut} → 200 con enlaces")
    void findByRut_ok() throws Exception {
        when(clienteService.findByRut(RUT)).thenReturn(respuesta());

        mockMvc.perform(get("/api/v1/clientes/{rut}", RUT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comuna").value("Concepción"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/clientes/" + RUT)));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{rut} inexistente → 404")
    void findByRut_noExiste() throws Exception {
        when(clienteService.findByRut(RUT))
                .thenThrow(ApiException.noEncontrado("No existe un cliente con el RUT " + RUT));

        mockMvc.perform(get("/api/v1/clientes/{rut}", RUT))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje", containsString(RUT)));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/buscar?nombre= → colección con las coincidencias")
    void buscarPorNombre() throws Exception {
        when(clienteService.buscarPorNombre("ped")).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/v1/clientes/buscar").param("nombre", "ped"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.clientes", hasSize(1)))
                .andExpect(jsonPath("$._embedded.clientes[0].rutUsuario").value(RUT));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/buscar sin coincidencias → 200 sin _embedded")
    void buscarPorNombre_vacio() throws Exception {
        when(clienteService.buscarPorNombre("zzz")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/clientes/buscar").param("nombre", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{rut} → 200 con el cliente actualizado")
    void actualizarDatos_ok() throws Exception {
        when(clienteService.actualizarDatos(eq(RUT), any(ClienteUpdateRequest.class)))
                .thenReturn(new ClienteResponse(RUT, "Matias Pablo", "Dominguez", null, "Otra calle 45", "Talcahuano"));
        ClienteUpdateRequest cuerpo = new ClienteUpdateRequest("Matias Pablo", "Dominguez", null, "Otra calle 45", "Talcahuano");

        mockMvc.perform(put("/api/v1/clientes/{rut}", RUT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuerpo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Matias Pablo"))
                .andExpect(jsonPath("$.comuna").value("Talcahuano"))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{rut} con nombre vacío → 400")
    void actualizarDatos_validacion() throws Exception {
        ClienteUpdateRequest cuerpo = new ClienteUpdateRequest("", "Dominguez", null, null, null);

        mockMvc.perform(put("/api/v1/clientes/{rut}", RUT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuerpo)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("nombre")));
    }
}
