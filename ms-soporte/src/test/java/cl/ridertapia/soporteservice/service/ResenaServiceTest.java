package cl.ridertapia.soporteservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.client.UsuarioDto;
import cl.ridertapia.soporteservice.dto.ResenaDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Resena;
import cl.ridertapia.soporteservice.repository.ResenaRepository;
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
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private UsuarioClient usuarioClient;

    @InjectMocks
    private ResenaService resenaService;

    private Resena resena(Long id, int calificacion, Resena.Estado estado) {
        return Resena.builder().idResena(id).rutUsuario("12345678-5").idProducto(100L)
                .calificacion(calificacion).comentario("ok").fecha(LocalDateTime.now()).estado(estado).build();
    }

    @Test
    @DisplayName("guardarResena: valida la cuenta y guarda la reseña como VISIBLE")
    void guardarResena_ok() {
        when(usuarioClient.obtenerPorRut("12345678-5")).thenReturn(new UsuarioDto("12345678-5", "a@b.cl", "CLIENTE"));
        when(resenaRepository.existsByRutUsuarioAndIdProducto("12345678-5", 100L)).thenReturn(false);
        when(resenaRepository.save(any(Resena.class))).thenAnswer(i -> {
            Resena r = i.getArgument(0);
            r.setIdResena(1L);
            return r;
        });

        ResenaDto.Respuesta respuesta = resenaService.guardarResena(
                new ResenaDto.Crear("12345678-5", 100L, 5, "Excelente"));

        assertThat(respuesta.idResena()).isEqualTo(1L);
        assertThat(respuesta.estado()).isEqualTo(Resena.Estado.VISIBLE);
        assertThat(respuesta.calificacion()).isEqualTo(5);
    }

    @Test
    @DisplayName("guardarResena: si el usuario ya reseñó el producto, responde 409 y no guarda")
    void guardarResena_duplicada() {
        when(resenaRepository.existsByRutUsuarioAndIdProducto("12345678-5", 100L)).thenReturn(true);

        assertThatThrownBy(() -> resenaService.guardarResena(new ResenaDto.Crear("12345678-5", 100L, 4, null)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(resenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("guardarResena: si usuario-service dice que no existe la cuenta, no guarda")
    void guardarResena_usuarioInexistente() {
        when(usuarioClient.obtenerPorRut("12345678-5")).thenThrow(ApiException.noEncontrado("sin cuenta"));

        assertThatThrownBy(() -> resenaService.guardarResena(new ResenaDto.Crear("12345678-5", 100L, 4, null)))
                .isInstanceOfSatisfying(ApiException.class, e -> assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        verify(resenaRepository, never()).save(any());
    }

    @Test
    @DisplayName("resumenProducto: promedia solo las reseñas visibles y redondea a 1 decimal")
    void resumenProducto_promedio() {
        when(resenaRepository.findByIdProductoAndEstado(100L, Resena.Estado.VISIBLE)).thenReturn(List.of(
                resena(1L, 5, Resena.Estado.VISIBLE), resena(2L, 4, Resena.Estado.VISIBLE), resena(3L, 4, Resena.Estado.VISIBLE)));

        ResenaDto.Resumen resumen = resenaService.resumenProducto(100L);

        assertThat(resumen.promedio()).isEqualTo(4.3);
        assertThat(resumen.totalResenas()).isEqualTo(3);
    }

    @Test
    @DisplayName("resumenProducto: sin reseñas el promedio es 0")
    void resumenProducto_sinResenas() {
        when(resenaRepository.findByIdProductoAndEstado(100L, Resena.Estado.VISIBLE)).thenReturn(List.of());

        ResenaDto.Resumen resumen = resenaService.resumenProducto(100L);

        assertThat(resumen.promedio()).isZero();
        assertThat(resumen.totalResenas()).isZero();
    }

    @Test
    @DisplayName("findByProducto: pide solo las visibles")
    void findByProducto_soloVisibles() {
        when(resenaRepository.findByIdProductoAndEstado(100L, Resena.Estado.VISIBLE))
                .thenReturn(List.of(resena(1L, 5, Resena.Estado.VISIBLE)));

        assertThat(resenaService.findByProducto(100L)).hasSize(1);
    }

    @Test
    @DisplayName("findById: devuelve la reseña o 404")
    void findById() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena(1L, 5, Resena.Estado.VISIBLE)));
        when(resenaRepository.findById(9L)).thenReturn(Optional.empty());

        assertThat(resenaService.findById(1L).idResena()).isEqualTo(1L);
        assertThatThrownBy(() -> resenaService.findById(9L))
                .isInstanceOfSatisfying(ApiException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(e.getMessage()).contains("9");
                });
    }

    @Test
    @DisplayName("ocultarResena: cambia el estado a OCULTA")
    void ocultarResena() {
        Resena r = resena(1L, 5, Resena.Estado.VISIBLE);
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(r));
        when(resenaRepository.save(r)).thenReturn(r);

        assertThat(resenaService.ocultarResena(1L).estado()).isEqualTo(Resena.Estado.OCULTA);
    }
}
