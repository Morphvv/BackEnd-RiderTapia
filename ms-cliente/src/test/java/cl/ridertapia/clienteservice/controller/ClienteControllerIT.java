package cl.ridertapia.clienteservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.clienteservice.client.UsuarioClient;
import cl.ridertapia.clienteservice.dto.ClienteRequest;
import cl.ridertapia.clienteservice.dto.ClienteUpdateRequest;
import cl.ridertapia.clienteservice.dto.UsuarioDto;
import cl.ridertapia.clienteservice.exception.ApiException;
import cl.ridertapia.clienteservice.model.Cliente;
import cl.ridertapia.clienteservice.repository.ClienteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@AutoConfigureTestDatabase // reemplaza MySQL por H2 solo en esta prueba
class ClienteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteRepository clienteRepository;

    @MockBean
    private UsuarioClient usuarioClient;

    @BeforeEach
    void limpiar() {
        clienteRepository.deleteAll();
    }

    @Test
    void testCrearYObtenerCliente() throws Exception {
        when(usuarioClient.obtenerPorRut("12345678-5"))
                .thenReturn(new UsuarioDto("12345678-5", "juan.perez@example.com", "CLIENTE"));
        ClienteRequest cliente = new ClienteRequest("12345678-5", "Juan", "Perez", "+56912345678", "Calle 1", "Concepción");

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cliente)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rutUsuario").value("12345678-5"))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$._links").exists());

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.clientes", hasSize(1)))
                .andExpect(jsonPath("$._embedded.clientes[0].nombre").value("Juan"))
                .andExpect(jsonPath("$._embedded.clientes[0].apellido").value("Perez"))
                .andExpect(jsonPath("$._embedded.clientes[0].telefono").value("+56912345678"))
                .andExpect(jsonPath("$._embedded.clientes[0].direccion").value("Calle 1"))
                .andExpect(jsonPath("$._embedded.clientes[0].comuna").value("Concepción"))
                .andExpect(jsonPath("$._links.self").exists());

        assertThat(clienteRepository.findById("12345678-5")).isPresent();
    }

    @Test
    void testObtenerPorRut() throws Exception {
        clienteRepository.save(Cliente.builder().rutUsuario("11111111-1").nombre("Maria").apellido("Gomez")
                .comuna("Talcahuano").build());

        mockMvc.perform(get("/api/v1/clientes/11111111-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Maria"))
                .andExpect(jsonPath("$.comuna").value("Talcahuano"))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void testObtenerClienteInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/clientes/12345678-5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje", containsString("12345678-5")));
    }

    @Test
    void testBuscarPorNombre() throws Exception {
        clienteRepository.save(Cliente.builder().rutUsuario("12345678-5").nombre("Matias").apellido("Dominguez").build());
        clienteRepository.save(Cliente.builder().rutUsuario("11111111-1").nombre("Ana").apellido("Matamala").build());
        clienteRepository.save(Cliente.builder().rutUsuario("12342313-5").nombre("Luis").apellido("Rojas").build());

        mockMvc.perform(get("/api/v1/clientes/buscar").param("nombre", "mat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.clientes", hasSize(2)));

        mockMvc.perform(get("/api/v1/clientes/buscar").param("nombre", "zzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void testActualizarCliente() throws Exception {
        clienteRepository.save(Cliente.builder().rutUsuario("12342313-5").nombre("Carlos").apellido("Lopez")
                .telefono("+56911111111").comuna("Concepción").build());

        ClienteUpdateRequest actualizado =
                new ClienteUpdateRequest("Carlos", "Lopez", "+56922222222", "Nueva calle 9", "Hualpén");

        mockMvc.perform(put("/api/v1/clientes/12342313-5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefono").value("+56922222222"))
                .andExpect(jsonPath("$.comuna").value("Hualpén"))
                .andExpect(jsonPath("$._links").exists());

        Cliente enBd = clienteRepository.findById("12342313-5").orElseThrow();
        assertThat(enBd.getDireccion()).isEqualTo("Nueva calle 9");
        assertThat(enBd.getComuna()).isEqualTo("Hualpén");
    }

    @Test
    void testCrearClienteDuplicado() throws Exception {
        when(usuarioClient.obtenerPorRut("12345678-5"))
                .thenReturn(new UsuarioDto("12345678-5", "juan.perez@example.com", "CLIENTE"));
        String json = objectMapper.writeValueAsString(
                new ClienteRequest("12345678-5", "Juan", "Perez", null, null, null));

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("12345678-5")));

        assertThat(clienteRepository.count()).isEqualTo(1);
    }

    @Test
    void testCrearClienteConCuentaAdmin() throws Exception {
        when(usuarioClient.obtenerPorRut("12345678-5"))
                .thenReturn(new UsuarioDto("12345678-5", "admin@example.com", "ADMIN"));
        String json = objectMapper.writeValueAsString(
                new ClienteRequest("12345678-5", "Juan", "Perez", null, null, null));

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("rol CLIENTE")));

        assertThat(clienteRepository.count()).isZero();
    }

    @Test
    void testCrearClienteSinCuentaDeUsuario() throws Exception {
        when(usuarioClient.obtenerPorRut("12345678-5"))
                .thenThrow(ApiException.noEncontrado("No existe una cuenta de usuario con el RUT 12345678-5"));
        String json = objectMapper.writeValueAsString(
                new ClienteRequest("12345678-5", "Juan", "Perez", null, null, null));

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isNotFound());

        assertThat(clienteRepository.count()).isZero();
    }

    @Test
    void testCrearClienteConUsuarioServiceCaido() throws Exception {
        when(usuarioClient.obtenerPorRut("12345678-5"))
                .thenThrow(ApiException.servicioNoDisponible("usuario-service no disponible"));
        String json = objectMapper.writeValueAsString(
                new ClienteRequest("12345678-5", "Juan", "Perez", null, null, null));

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isServiceUnavailable());

        assertThat(clienteRepository.count()).isZero();
    }

    @Test
    void testCrearClienteConDatosInvalidos() throws Exception {
        String json = objectMapper.writeValueAsString(new ClienteRequest("123", "", "", "abc", null, null));

        mockMvc.perform(post("/api/v1/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("rutUsuario")));
    }
}
