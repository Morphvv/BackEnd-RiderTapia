package cl.ridertapia.clienteservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.ridertapia.clienteservice.client.UsuarioClient;
import cl.ridertapia.clienteservice.dto.ClienteRequest;
import cl.ridertapia.clienteservice.dto.ClienteResponse;
import cl.ridertapia.clienteservice.dto.ClienteUpdateRequest;
import cl.ridertapia.clienteservice.dto.UsuarioDto;
import cl.ridertapia.clienteservice.exception.ApiException;
import cl.ridertapia.clienteservice.model.Cliente;
import cl.ridertapia.clienteservice.repository.ClienteRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    private static final String RUT = "12345678-5";

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente() {
        return Cliente.builder().rutUsuario(RUT).nombre("Matias").apellido("Dominguez")
                .telefono("+56912345678").direccion("Calle Falsa 123").comuna("Concepción").build();
    }

    private ClienteRequest request() {
        return new ClienteRequest(RUT, "  Matias ", " Dominguez  ", "+56912345678", "Calle Falsa 123", "Concepción");
    }

    private void assertEstado(Throwable ex, HttpStatus esperado) {
        assertThat(ex).isInstanceOf(ApiException.class);
        assertThat(((ApiException) ex).getStatus()).isEqualTo(esperado);
    }

    // ---------- guardarCliente ----------

    @Test
    @DisplayName("guardarCliente: crea el perfil si el RUT tiene cuenta con rol CLIENTE")
    void guardarCliente_ok() {
        when(clienteRepository.existsById(RUT)).thenReturn(false);
        when(usuarioClient.obtenerPorRut(RUT)).thenReturn(new UsuarioDto(RUT, "Matias@correo.cl", "CLIENTE"));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse respuesta = clienteService.guardarCliente(request());

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getNombre()).isEqualTo("Matias");     // sin espacios
        assertThat(captor.getValue().getApellido()).isEqualTo("Dominguez");
        assertThat(respuesta.rutUsuario()).isEqualTo(RUT);
        assertThat(respuesta.comuna()).isEqualTo("Concepción");
    }

    @Test
    @DisplayName("guardarCliente: normaliza la K del RUT antes de consultar")
    void guardarCliente_rutConK() {
        when(clienteRepository.existsById("10000013-K")).thenReturn(false);
        when(usuarioClient.obtenerPorRut("10000013-K")).thenReturn(new UsuarioDto("10000013-K", "k@correo.cl", "CLIENTE"));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse respuesta = clienteService.guardarCliente(
                new ClienteRequest("10000013-k", "Ana", "Pérez", null, null, null));

        assertThat(respuesta.rutUsuario()).isEqualTo("10000013-K");
    }

    @Test
    @DisplayName("guardarCliente: 409 si ya existe el perfil (no consulta a usuario-service)")
    void guardarCliente_duplicado() {
        when(clienteRepository.existsById(RUT)).thenReturn(true);
        ClienteRequest request = request();

        assertThatThrownBy(() -> clienteService.guardarCliente(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.CONFLICT));
        verify(usuarioClient, never()).obtenerPorRut(any());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarCliente: 400 si la cuenta no tiene rol CLIENTE")
    void guardarCliente_rolIncorrecto() {
        when(clienteRepository.existsById(RUT)).thenReturn(false);
        when(usuarioClient.obtenerPorRut(RUT)).thenReturn(new UsuarioDto(RUT, "admin@correo.cl", "ADMIN"));
        ClienteRequest request = request();

        assertThatThrownBy(() -> clienteService.guardarCliente(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.BAD_REQUEST));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarCliente: propaga el 404 cuando no existe la cuenta de usuario")
    void guardarCliente_sinCuenta() {
        when(clienteRepository.existsById(RUT)).thenReturn(false);
        when(usuarioClient.obtenerPorRut(RUT)).thenThrow(ApiException.noEncontrado("No existe una cuenta"));
        ClienteRequest request = request();

        assertThatThrownBy(() -> clienteService.guardarCliente(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarCliente: propaga el 503 cuando usuario-service no responde")
    void guardarCliente_usuarioServiceCaido() {
        when(clienteRepository.existsById(RUT)).thenReturn(false);
        when(usuarioClient.obtenerPorRut(RUT)).thenThrow(ApiException.servicioNoDisponible("caído"));
        ClienteRequest request = request();

        assertThatThrownBy(() -> clienteService.guardarCliente(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.SERVICE_UNAVAILABLE));
        verify(clienteRepository, never()).save(any());
    }

    // ---------- consultas ----------

    @Test
    @DisplayName("listarClientes: devuelve todos")
    void listarClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of(cliente()));

        assertThat(clienteService.listarClientes()).hasSize(1)
                .first().extracting(ClienteResponse::nombre).isEqualTo("Matias");
    }

    @Test
    @DisplayName("findByRut: devuelve el cliente si existe")
    void findByRut_ok() {
        when(clienteRepository.findById(RUT)).thenReturn(Optional.of(cliente()));

        assertThat(clienteService.findByRut(" " + RUT + " ").apellido()).isEqualTo("Dominguez");
    }

    @Test
    @DisplayName("findByRut: 404 si no existe")
    void findByRut_noExiste() {
        when(clienteRepository.findById(RUT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findByRut(RUT))
                .satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("buscarPorNombre: busca el texto sin espacios en nombre y apellido")
    void buscarPorNombre() {
        when(clienteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("ped", "ped"))
                .thenReturn(List.of(cliente()));

        List<ClienteResponse> resultado = clienteService.buscarPorNombre("  ped ");

        assertThat(resultado).extracting(ClienteResponse::rutUsuario).containsExactly(RUT);
    }

    @Test
    @DisplayName("buscarPorNombre: sin coincidencias devuelve lista vacía")
    void buscarPorNombre_vacio() {
        when(clienteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("zzz", "zzz"))
                .thenReturn(List.of());

        assertThat(clienteService.buscarPorNombre("zzz")).isEmpty();
    }

    // ---------- actualizarDatos ----------

    @Test
    @DisplayName("actualizarDatos: reemplaza los datos editables y conserva el RUT")
    void actualizarDatos_ok() {
        Cliente existente = cliente();
        when(clienteRepository.findById(RUT)).thenReturn(Optional.of(existente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse respuesta = clienteService.actualizarDatos(RUT,
                new ClienteUpdateRequest(" Matias Pablo ", "Dominguez", "+56987654321", "Otra calle 45", "Talcahuano"));

        assertThat(respuesta.rutUsuario()).isEqualTo(RUT);
        assertThat(respuesta.nombre()).isEqualTo("Matias Pablo");
        assertThat(respuesta.telefono()).isEqualTo("+56987654321");
        assertThat(respuesta.comuna()).isEqualTo("Talcahuano");
    }

    @Test
    @DisplayName("actualizarDatos: 404 si el cliente no existe")
    void actualizarDatos_noExiste() {
        when(clienteRepository.findById(RUT)).thenReturn(Optional.empty());
        ClienteUpdateRequest request = new ClienteUpdateRequest("Matias", "Dominguez", null, null, null);

        assertThatThrownBy(() -> clienteService.actualizarDatos(RUT, request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
        verify(clienteRepository, never()).save(any());
    }
}
