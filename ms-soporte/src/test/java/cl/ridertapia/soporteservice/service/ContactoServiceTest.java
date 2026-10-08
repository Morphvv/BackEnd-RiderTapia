package cl.ridertapia.soporteservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.dto.ContactoDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Contacto;
import cl.ridertapia.soporteservice.repository.ContactoRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ContactoServiceTest {

    @Mock
    private ContactoRepository contactoRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private ContactoService contactoService;

    private Contacto contacto(Contacto.Estado estado) {
        return Contacto.builder().idContacto(1L).nombre("Juan").emailContacto("juan@example.com")
                .asunto("Duda").mensaje("Hola").estado(estado).fecha(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("enviarContacto: sin RUT no consulta a usuario-service (visitante)")
    void enviar_sinRut() {
        when(contactoRepository.save(any(Contacto.class))).thenAnswer(i -> i.getArgument(0));

        ContactoDto.Respuesta r = contactoService.enviarContacto(
                new ContactoDto.Crear(null, " Juan ", "JUAN@Example.com ", " Duda ", " Hola "));

        assertThat(r.rutUsuario()).isNull();
        assertThat(r.nombre()).isEqualTo("Juan");
        assertThat(r.emailContacto()).isEqualTo("juan@example.com");
        assertThat(r.estado()).isEqualTo(Contacto.Estado.ABIERTO);
        verifyNoInteractions(usuarioClient);
    }

    @Test
    @DisplayName("enviarContacto: con RUT valida que exista la cuenta")
    void enviar_conRut() {
        when(contactoRepository.save(any(Contacto.class))).thenAnswer(i -> i.getArgument(0));

        ContactoDto.Respuesta r = contactoService.enviarContacto(
                new ContactoDto.Crear("12345678-5", "Juan", "juan@example.com", "Duda", "Hola"));

        assertThat(r.rutUsuario()).isEqualTo("12345678-5");
        verify(usuarioClient).obtenerPorRut("12345678-5");
    }

    @Test
    @DisplayName("enviarContacto: RUT sin cuenta responde 404 y no guarda")
    void enviar_rutSinCuenta() {
        when(usuarioClient.obtenerPorRut("12345678-5")).thenThrow(ApiException.noEncontrado("sin cuenta"));

        assertThatThrownBy(() -> contactoService.enviarContacto(
                new ContactoDto.Crear("12345678-5", "Juan", "juan@example.com", "Duda", "Hola")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(contactoRepository, never()).save(any());
    }

    @Test
    @DisplayName("responderContacto: guarda la respuesta y pasa a RESPONDIDO")
    void responder() {
        Contacto c = contacto(Contacto.Estado.ABIERTO);
        when(contactoRepository.findById(1L)).thenReturn(Optional.of(c));
        when(contactoRepository.save(c)).thenReturn(c);

        ContactoDto.Respuesta r = contactoService.responderContacto(1L, new ContactoDto.Responder("  Claro que sí  "));

        assertThat(r.estado()).isEqualTo(Contacto.Estado.RESPONDIDO);
        assertThat(r.respuesta()).isEqualTo("Claro que sí");
    }

    @Test
    @DisplayName("responderContacto: inexistente responde 404")
    void responder_noExiste() {
        when(contactoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactoService.responderContacto(9L, new ContactoDto.Responder("x")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("listarContactos: sin estado trae todos, con estado filtra")
    void listar() {
        when(contactoRepository.findAll()).thenReturn(List.of(contacto(Contacto.Estado.ABIERTO), contacto(Contacto.Estado.RESPONDIDO)));
        when(contactoRepository.findByEstado(Contacto.Estado.ABIERTO)).thenReturn(List.of(contacto(Contacto.Estado.ABIERTO)));

        assertThat(contactoService.listarContactos(null)).hasSize(2);
        assertThat(contactoService.listarContactos(Contacto.Estado.ABIERTO)).hasSize(1);
    }
}
