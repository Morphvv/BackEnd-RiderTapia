package cl.ridertapia.soporteservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.dto.SeguimientoDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Seguimiento;
import cl.ridertapia.soporteservice.repository.SeguimientoRepository;
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
class SeguimientoServiceTest {

    @Mock
    private SeguimientoRepository seguimientoRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private SeguimientoService seguimientoService;

    private Seguimiento seguimiento(Seguimiento.Estado estado, String observacion) {
        return Seguimiento.builder().idSeguimiento(1L).idPedido(1L).rutUsuario("12345678-5")
                .tipo(Seguimiento.Tipo.LLAMADA).estado(estado).observacion(observacion).fecha(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("registrarSeguimiento: queda PENDIENTE")
    void registrar() {
        when(seguimientoRepository.save(any(Seguimiento.class))).thenAnswer(i -> i.getArgument(0));

        SeguimientoDto.Respuesta r = seguimientoService.registrarSeguimiento(
                new SeguimientoDto.Crear(1L, "12345678-5", Seguimiento.Tipo.ENCUESTA, "Llamar el lunes"));

        assertThat(r.estado()).isEqualTo(Seguimiento.Estado.PENDIENTE);
        assertThat(r.tipo()).isEqualTo(Seguimiento.Tipo.ENCUESTA);
    }

    @Test
    @DisplayName("actualizarEstado: cambia el estado y conserva la observación si no llega una nueva")
    void actualizar_conservaObservacion() {
        Seguimiento s = seguimiento(Seguimiento.Estado.PENDIENTE, "original");
        when(seguimientoRepository.findById(1L)).thenReturn(Optional.of(s));
        when(seguimientoRepository.save(s)).thenReturn(s);

        SeguimientoDto.Respuesta r = seguimientoService.actualizarEstado(1L,
                new SeguimientoDto.ActualizarEstado(Seguimiento.Estado.REALIZADO, null));

        assertThat(r.estado()).isEqualTo(Seguimiento.Estado.REALIZADO);
        assertThat(r.observacion()).isEqualTo("original");
    }

    @Test
    @DisplayName("actualizarEstado: reemplaza la observación cuando llega una nueva")
    void actualizar_nuevaObservacion() {
        Seguimiento s = seguimiento(Seguimiento.Estado.PENDIENTE, "original");
        when(seguimientoRepository.findById(1L)).thenReturn(Optional.of(s));
        when(seguimientoRepository.save(s)).thenReturn(s);

        SeguimientoDto.Respuesta r = seguimientoService.actualizarEstado(1L,
                new SeguimientoDto.ActualizarEstado(Seguimiento.Estado.CANCELADO, " No contestó "));

        assertThat(r.observacion()).isEqualTo("No contestó");
    }

    @Test
    @DisplayName("findById y actualizarEstado: inexistente responde 404")
    void noExiste() {
        when(seguimientoRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seguimientoService.findById(9L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> seguimientoService.actualizarEstado(9L,
                new SeguimientoDto.ActualizarEstado(Seguimiento.Estado.REALIZADO, null)))
                .isInstanceOf(ApiException.class);
    }

    @Test
    @DisplayName("findByPedido: devuelve los seguimientos del pedido")
    void findByPedido() {
        when(seguimientoRepository.findByIdPedido(1L)).thenReturn(List.of(seguimiento(Seguimiento.Estado.PENDIENTE, null)));

        assertThat(seguimientoService.findByPedido(1L)).hasSize(1);
    }
}
