package cl.ridertapia.soporteservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.dto.DevolucionDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Devolucion;
import cl.ridertapia.soporteservice.repository.DevolucionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DevolucionServiceTest {

    @Mock
    private DevolucionRepository devolucionRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private DevolucionService devolucionService;

    private Devolucion devolucion(Devolucion.Estado estado) {
        return Devolucion.builder().idDevolucion(1L).rutUsuario("12345678-5").idPedido(1L).idProducto(100L)
                .motivo("Llegó con la llanta dañada").estado(estado).fechaSolicitud(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("solicitarDevolucion: queda SOLICITADA")
    void solicitar_ok() {
        when(devolucionRepository.existsByIdPedidoAndIdProductoAndEstadoIn(any(), any(), anyList())).thenReturn(false);
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(i -> i.getArgument(0));

        DevolucionDto.Respuesta r = devolucionService.solicitarDevolucion(
                new DevolucionDto.Crear("12345678-5", 1L, 100L, "  Llegó con la llanta dañada  "));

        assertThat(r.estado()).isEqualTo(Devolucion.Estado.SOLICITADA);
        assertThat(r.motivo()).isEqualTo("Llegó con la llanta dañada");
        assertThat(r.fechaResolucion()).isNull();
    }

    @Test
    @DisplayName("solicitarDevolucion: con otra en curso responde 409")
    void solicitar_enCurso() {
        when(devolucionRepository.existsByIdPedidoAndIdProductoAndEstadoIn(any(), any(), anyList())).thenReturn(true);

        assertThatThrownBy(() -> devolucionService.solicitarDevolucion(
                new DevolucionDto.Crear("12345678-5", 1L, 100L, "Llegó con la llanta dañada")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(devolucionRepository, never()).save(any());
    }

    @Test
    @DisplayName("resolverDevolucion: SOLICITADA pasa a APROBADA y registra la fecha")
    void resolver_aprobar() {
        Devolucion d = devolucion(Devolucion.Estado.SOLICITADA);
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(d));
        when(devolucionRepository.save(d)).thenReturn(d);

        DevolucionDto.Respuesta r = devolucionService.resolverDevolucion(1L,
                new DevolucionDto.Resolver(Devolucion.Estado.APROBADA, "Envíalo a nuestra tienda"));

        assertThat(r.estado()).isEqualTo(Devolucion.Estado.APROBADA);
        assertThat(r.fechaResolucion()).isNotNull();
        assertThat(r.respuesta()).isEqualTo("Envíalo a nuestra tienda");
    }

    @Test
    @DisplayName("resolverDevolucion: APROBADA pasa a COMPLETADA sin exigir respuesta")
    void resolver_completar() {
        Devolucion d = devolucion(Devolucion.Estado.APROBADA);
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(d));
        when(devolucionRepository.save(d)).thenReturn(d);

        assertThat(devolucionService.resolverDevolucion(1L, new DevolucionDto.Resolver(Devolucion.Estado.COMPLETADA, null))
                .estado()).isEqualTo(Devolucion.Estado.COMPLETADA);
    }

    @Test
    @DisplayName("resolverDevolucion: rechazar sin explicar el motivo responde 400")
    void resolver_rechazoSinMotivo() {
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(devolucion(Devolucion.Estado.SOLICITADA)));

        assertThatThrownBy(() -> devolucionService.resolverDevolucion(1L,
                new DevolucionDto.Resolver(Devolucion.Estado.RECHAZADA, "  ")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(devolucionRepository, never()).save(any());
    }

    @Test
    @DisplayName("resolverDevolucion: rechazar con motivo funciona")
    void resolver_rechazoConMotivo() {
        Devolucion d = devolucion(Devolucion.Estado.SOLICITADA);
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(d));
        when(devolucionRepository.save(d)).thenReturn(d);

        DevolucionDto.Respuesta r = devolucionService.resolverDevolucion(1L,
                new DevolucionDto.Resolver(Devolucion.Estado.RECHAZADA, "Fuera de plazo"));

        assertThat(r.estado()).isEqualTo(Devolucion.Estado.RECHAZADA);
    }

    @ParameterizedTest(name = "{0} -> {1} no está permitido")
    @CsvSource({
            "SOLICITADA, COMPLETADA",
            "SOLICITADA, SOLICITADA",
            "APROBADA, RECHAZADA",
            "APROBADA, APROBADA",
            "RECHAZADA, APROBADA",
            "COMPLETADA, APROBADA"
    })
    @DisplayName("resolverDevolucion: transiciones inválidas responden 409")
    void resolver_transicionInvalida(Devolucion.Estado actual, Devolucion.Estado nuevo) {
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(devolucion(actual)));

        assertThatThrownBy(() -> devolucionService.resolverDevolucion(1L, new DevolucionDto.Resolver(nuevo, "x")))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(devolucionRepository, never()).save(any());
    }

    @Test
    @DisplayName("listarDevoluciones: sin estado trae todas, con estado filtra")
    void listar() {
        when(devolucionRepository.findAll()).thenReturn(List.of(devolucion(Devolucion.Estado.SOLICITADA), devolucion(Devolucion.Estado.APROBADA)));
        when(devolucionRepository.findByEstado(Devolucion.Estado.APROBADA)).thenReturn(List.of(devolucion(Devolucion.Estado.APROBADA)));

        assertThat(devolucionService.listarDevoluciones(null)).hasSize(2);
        assertThat(devolucionService.listarDevoluciones(Devolucion.Estado.APROBADA)).hasSize(1);
    }

    @Test
    @DisplayName("findById: inexistente responde 404")
    void findById_noExiste() {
        when(devolucionRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> devolucionService.findById(9L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
