package cl.ridertapia.usuarioservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.ridertapia.usuarioservice.dto.LoginRequest;
import cl.ridertapia.usuarioservice.dto.RecuperarPasswordRequest;
import cl.ridertapia.usuarioservice.dto.UsuarioRequest;
import cl.ridertapia.usuarioservice.dto.UsuarioResponse;
import cl.ridertapia.usuarioservice.dto.UsuarioUpdateRequest;
import cl.ridertapia.usuarioservice.exception.ApiException;
import cl.ridertapia.usuarioservice.model.Usuario;
import cl.ridertapia.usuarioservice.repository.UsuarioRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String RUT = "12345678-5";
    private static final String EMAIL = "matias@correo.cl";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioGuardado() {
        return Usuario.builder().rutUsuario(RUT).emailCliente(EMAIL).password("hash").rol("CLIENTE").build();
    }

    private void assertEstado(Throwable ex, HttpStatus esperado) {
        assertThat(ex).isInstanceOf(ApiException.class);
        assertThat(((ApiException) ex).getStatus()).isEqualTo(esperado);
    }

    // ---------- guardarUsuario ----------

    @Test
    @DisplayName("guardarUsuario: crea el usuario con rol CLIENTE, email en minúscula y clave cifrada")
    void guardarUsuario_ok() {
        when(usuarioRepository.existsById(RUT)).thenReturn(false);
        when(usuarioRepository.existsByEmailCliente(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode("clave12345")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = usuarioService.guardarUsuario(
                new UsuarioRequest(RUT, "  Matias@Correo.CL ", "clave12345", null));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hash");
        assertThat(captor.getValue().getEmailCliente()).isEqualTo(EMAIL);
        assertThat(respuesta).isEqualTo(new UsuarioResponse(RUT, EMAIL, "CLIENTE"));
    }

    @Test
    @DisplayName("guardarUsuario: respeta el rol ADMIN y normaliza la K del RUT")
    void guardarUsuario_adminYRutConK() {
        when(usuarioRepository.existsById("10000013-K")).thenReturn(false);
        when(usuarioRepository.existsByEmailCliente(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode("clave12345")).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = usuarioService.guardarUsuario(
                new UsuarioRequest("10000013-k", EMAIL, "clave12345", "ADMIN"));

        assertThat(respuesta.rutUsuario()).isEqualTo("10000013-K");
        assertThat(respuesta.rol()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("guardarUsuario: rechaza un RUT con dígito verificador incorrecto")
    void guardarUsuario_rutInvalido() {
        UsuarioRequest request = new UsuarioRequest("12345678-9", EMAIL, "clave12345", null);

        assertThatThrownBy(() -> usuarioService.guardarUsuario(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.BAD_REQUEST));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarUsuario: 409 si el RUT ya existe")
    void guardarUsuario_rutDuplicado() {
        when(usuarioRepository.existsById(RUT)).thenReturn(true);
        UsuarioRequest request = new UsuarioRequest(RUT, EMAIL, "clave12345", null);

        assertThatThrownBy(() -> usuarioService.guardarUsuario(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.CONFLICT));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarUsuario: 409 si el email ya existe")
    void guardarUsuario_emailDuplicado() {
        when(usuarioRepository.existsById(RUT)).thenReturn(false);
        when(usuarioRepository.existsByEmailCliente(EMAIL)).thenReturn(true);
        UsuarioRequest request = new UsuarioRequest(RUT, EMAIL, "clave12345", null);

        assertThatThrownBy(() -> usuarioService.guardarUsuario(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.CONFLICT));
        verify(usuarioRepository, never()).save(any());
    }

    // ---------- consultas ----------

    @Test
    @DisplayName("listarUsuarios: devuelve todos sin exponer la contraseña")
    void listarUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioGuardado()));

        List<UsuarioResponse> lista = usuarioService.listarUsuarios();

        assertThat(lista).containsExactly(new UsuarioResponse(RUT, EMAIL, "CLIENTE"));
    }

    @Test
    @DisplayName("findByRut: devuelve el usuario si existe")
    void findByRut_ok() {
        when(usuarioRepository.findById(RUT)).thenReturn(Optional.of(usuarioGuardado()));

        assertThat(usuarioService.findByRut(RUT).emailCliente()).isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("findByRut: 404 si no existe")
    void findByRut_noExiste() {
        when(usuarioRepository.findById(RUT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findByRut(RUT))
                .satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("findByEmailCliente: busca ignorando mayúsculas y espacios")
    void findByEmail_ok() {
        when(usuarioRepository.findByEmailCliente(EMAIL)).thenReturn(Optional.of(usuarioGuardado()));

        assertThat(usuarioService.findByEmailCliente(" MATIAS@correo.cl ").rutUsuario()).isEqualTo(RUT);
    }

    @Test
    @DisplayName("findByEmailCliente: 404 si no existe")
    void findByEmail_noExiste() {
        when(usuarioRepository.findByEmailCliente(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.findByEmailCliente(EMAIL))
                .satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
    }

    // ---------- actualizarUsuario ----------

    @Test
    @DisplayName("actualizarUsuario: sin clave ni rol, mantiene los actuales")
    void actualizar_soloEmail() {
        Usuario existente = usuarioGuardado();
        when(usuarioRepository.findById(RUT)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByEmailCliente("nuevo@correo.cl")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = usuarioService.actualizarUsuario(RUT,
                new UsuarioUpdateRequest("Nuevo@correo.cl", null, null));

        assertThat(respuesta.emailCliente()).isEqualTo("nuevo@correo.cl");
        assertThat(existente.getPassword()).isEqualTo("hash");
        assertThat(existente.getRol()).isEqualTo("CLIENTE");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("actualizarUsuario: cambia clave (cifrada) y rol cuando se envían")
    void actualizar_claveYRol() {
        Usuario existente = usuarioGuardado();
        when(usuarioRepository.findById(RUT)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("otraClave123")).thenReturn("hashNuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse respuesta = usuarioService.actualizarUsuario(RUT,
                new UsuarioUpdateRequest(EMAIL, "otraClave123", "ADMIN"));

        assertThat(existente.getPassword()).isEqualTo("hashNuevo");
        assertThat(respuesta.rol()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("actualizarUsuario: 409 si el email nuevo pertenece a otro usuario")
    void actualizar_emailDeOtro() {
        when(usuarioRepository.findById(RUT)).thenReturn(Optional.of(usuarioGuardado()));
        when(usuarioRepository.existsByEmailCliente("otro@correo.cl")).thenReturn(true);
        UsuarioUpdateRequest request = new UsuarioUpdateRequest("otro@correo.cl", null, null);

        assertThatThrownBy(() -> usuarioService.actualizarUsuario(RUT, request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.CONFLICT));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("actualizarUsuario: 404 si el usuario no existe")
    void actualizar_noExiste() {
        when(usuarioRepository.findById(RUT)).thenReturn(Optional.empty());
        UsuarioUpdateRequest request = new UsuarioUpdateRequest(EMAIL, null, null);

        assertThatThrownBy(() -> usuarioService.actualizarUsuario(RUT, request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
    }

    // ---------- iniciarSesion ----------

    @Test
    @DisplayName("iniciarSesion: credenciales correctas")
    void login_ok() {
        when(usuarioRepository.findByEmailCliente(EMAIL)).thenReturn(Optional.of(usuarioGuardado()));
        when(passwordEncoder.matches("clave12345", "hash")).thenReturn(true);

        UsuarioResponse respuesta = usuarioService.iniciarSesion(new LoginRequest(EMAIL, "clave12345"));

        assertThat(respuesta.rutUsuario()).isEqualTo(RUT);
    }

    @Test
    @DisplayName("iniciarSesion: clave incorrecta devuelve 401")
    void login_claveIncorrecta() {
        when(usuarioRepository.findByEmailCliente(EMAIL)).thenReturn(Optional.of(usuarioGuardado()));
        when(passwordEncoder.matches("mala", "hash")).thenReturn(false);
        LoginRequest request = new LoginRequest(EMAIL, "mala");

        assertThatThrownBy(() -> usuarioService.iniciarSesion(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("iniciarSesion: email inexistente devuelve 401 con el mismo mensaje que clave mala")
    void login_emailInexistente() {
        when(usuarioRepository.findByEmailCliente(EMAIL)).thenReturn(Optional.empty());
        LoginRequest request = new LoginRequest(EMAIL, "clave12345");

        assertThatThrownBy(() -> usuarioService.iniciarSesion(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.UNAUTHORIZED))
                .hasMessage("Email o contraseña incorrectos");
    }

    // ---------- recuperarPassword ----------

    @Test
    @DisplayName("recuperarPassword: guarda la nueva clave cifrada")
    void recuperar_ok() {
        Usuario existente = usuarioGuardado();
        when(usuarioRepository.findByEmailCliente(EMAIL)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("nuevaClave123")).thenReturn("hashNuevo");

        usuarioService.recuperarPassword(new RecuperarPasswordRequest(EMAIL, "nuevaClave123"));

        assertThat(existente.getPassword()).isEqualTo("hashNuevo");
        verify(usuarioRepository).save(existente);
    }

    @Test
    @DisplayName("recuperarPassword: 404 si el email no existe")
    void recuperar_noExiste() {
        when(usuarioRepository.findByEmailCliente(EMAIL)).thenReturn(Optional.empty());
        RecuperarPasswordRequest request = new RecuperarPasswordRequest(EMAIL, "nuevaClave123");

        assertThatThrownBy(() -> usuarioService.recuperarPassword(request))
                .satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
        verify(usuarioRepository, never()).save(any());
    }
}
