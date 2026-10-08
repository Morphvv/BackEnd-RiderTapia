package cl.ridertapia.soporteservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.dto.GarantiaDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Garantia;
import cl.ridertapia.soporteservice.repository.GarantiaRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class GarantiaServiceTest {

    @Mock
    private GarantiaRepository garantiaRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private GarantiaService garantiaService;

    private Garantia garantia(LocalDate termino, Garantia.Estado estado) {
        return Garantia.builder().idGarantia(1L).rutUsuario("12345678-5").idPedido(1L).idProducto(100L)
                .fechaInicio(termino.minusMonths(12)).fechaTermino(termino).estado(estado).build();
    }

    @Test
    @DisplayName("registrarGarantia: sin meses usa 12 de cobertura")
    void registrar_12MesesPorDefecto() {
        when(garantiaRepository.existsByIdPedidoAndIdProducto(1L, 100L)).thenReturn(false);
        when(garantiaRepository.save(any(Garantia.class))).thenAnswer(i -> i.getArgument(0));

        GarantiaDto.Respuesta r = garantiaService.registrarGarantia(new GarantiaDto.Crear("12345678-5", 1L, 100L, null));

        assertThat(r.fechaInicio()).isEqualTo(LocalDate.now());
        assertThat(r.fechaTermino()).isEqualTo(LocalDate.now().plusMonths(12));
        assertThat(r.estado()).isEqualTo(Garantia.Estado.VIGENTE);
    }

    @Test
    @DisplayName("registrarGarantia: respeta los meses indicados")
    void registrar_mesesIndicados() {
        when(garantiaRepository.save(any(Garantia.class))).thenAnswer(i -> i.getArgument(0));

        GarantiaDto.Respuesta r = garantiaService.registrarGarantia(new GarantiaDto.Crear("12345678-5", 1L, 100L, 24));

        assertThat(r.fechaTermino()).isEqualTo(LocalDate.now().plusMonths(24));
    }

    @Test
    @DisplayName("registrarGarantia: duplicada para el mismo producto del pedido responde 409")
    void registrar_duplicada() {
        when(garantiaRepository.existsByIdPedidoAndIdProducto(1L, 100L)).thenReturn(true);

        assertThatThrownBy(() -> garantiaService.registrarGarantia(new GarantiaDto.Crear("12345678-5", 1L, 100L, null)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(garantiaRepository, never()).save(any());
    }

    @Test
    @DisplayName("verificarVigencia: garantía al día informa los días restantes y no la modifica")
    void vigencia_vigente() {
        when(garantiaRepository.findById(1L)).thenReturn(Optional.of(garantia(LocalDate.now().plusDays(30), Garantia.Estado.VIGENTE)));

        GarantiaDto.Vigencia v = garantiaService.verificarVigencia(1L);

        assertThat(v.vigente()).isTrue();
        assertThat(v.diasRestantes()).isEqualTo(30);
        verify(garantiaRepository, never()).save(any());
    }

    @Test
    @DisplayName("verificarVigencia: el último día todavía está vigente")
    void vigencia_ultimoDia() {
        when(garantiaRepository.findById(1L)).thenReturn(Optional.of(garantia(LocalDate.now(), Garantia.Estado.VIGENTE)));

        GarantiaDto.Vigencia v = garantiaService.verificarVigencia(1L);

        assertThat(v.vigente()).isTrue();
        assertThat(v.diasRestantes()).isZero();
    }

    @Test
    @DisplayName("verificarVigencia: si venció la marca como EXPIRADA")
    void vigencia_vencida() {
        Garantia g = garantia(LocalDate.now().minusDays(1), Garantia.Estado.VIGENTE);
        when(garantiaRepository.findById(1L)).thenReturn(Optional.of(g));

        GarantiaDto.Vigencia v = garantiaService.verificarVigencia(1L);

        assertThat(v.vigente()).isFalse();
        assertThat(v.diasRestantes()).isZero();
        assertThat(g.getEstado()).isEqualTo(Garantia.Estado.EXPIRADA);
        verify(garantiaRepository).save(g);
    }

    @Test
    @DisplayName("verificarVigencia: garantía inexistente responde 404")
    void vigencia_noExiste() {
        when(garantiaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> garantiaService.verificarVigencia(9L))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
