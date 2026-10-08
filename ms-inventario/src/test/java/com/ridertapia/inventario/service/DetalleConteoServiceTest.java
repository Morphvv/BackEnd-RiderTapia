package com.ridertapia.inventario.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ridertapia.inventario.model.DetalleConteo;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.model.EstadoToma;
import com.ridertapia.inventario.repository.DetalleConteoRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;
import com.ridertapia.inventario.repository.TomaInventarioRepository;

@ExtendWith(MockitoExtension.class)
class DetalleConteoServiceTest {

    @Mock
    private DetalleConteoRepository detalleConteoRepository;

    @Mock
    private TomaInventarioRepository tomaInventarioRepository;

    @Mock
    private ItemInventarioRepository itemInventarioRepository;

    @InjectMocks
    private DetalleConteoService detalleConteoService;

    private DetalleConteo detalleConteo;

    @BeforeEach
    void setUp() {
        detalleConteo = new DetalleConteo(1L, 15, 14, -1, new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L), new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    @Test
    void guardarDetalleConteo() {
        when(tomaInventarioRepository.findById(1L)).thenReturn(Optional.of(new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L)));
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(detalleConteoRepository.save(detalleConteo)).thenReturn(detalleConteo);

        DetalleConteo resultado = detalleConteoService.guardarDetalleConteo(detalleConteo);

        assertNotNull(resultado);
        assertEquals(14, resultado.getCantidadContada());
        verify(detalleConteoRepository, times(1)).save(detalleConteo);
    }

    @Test
    void guardarDetalleConteoConTomaInventarioNoExistente() {
        detalleConteo.setTomaInventario(new TomaInventario(99L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L));
        when(tomaInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> detalleConteoService.guardarDetalleConteo(detalleConteo));

        assertEquals("TomaInventario no encontrada con id: 99", ex.getMessage());
        verify(detalleConteoRepository, never()).save(any(DetalleConteo.class));
    }

    @Test
    void listarDetallesConteo() {
        DetalleConteo otro = new DetalleConteo(2L, 8, 8, 0, new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L), new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(detalleConteoRepository.findAll()).thenReturn(Arrays.asList(detalleConteo, otro));

        List<DetalleConteo> resultado = detalleConteoService.listarDetallesConteo();

        assertEquals(2, resultado.size());
        verify(detalleConteoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(detalleConteoRepository.findById(1L)).thenReturn(Optional.of(detalleConteo));

        Optional<DetalleConteo> resultado = detalleConteoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(14, resultado.get().getCantidadContada());
        verify(detalleConteoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(detalleConteoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<DetalleConteo> resultado = detalleConteoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(detalleConteoRepository, times(1)).findById(99L);
    }

    @Test
    void findByTomaInventarioConResultados() {
        when(detalleConteoRepository.findByTomaInventarioIdTomaInventario(1L)).thenReturn(List.of(detalleConteo));

        List<DetalleConteo> resultado = detalleConteoService.findByTomaInventario(1L);

        assertEquals(1, resultado.size());
        verify(detalleConteoRepository, times(1)).findByTomaInventarioIdTomaInventario(1L);
    }

    @Test
    void findByTomaInventarioSinResultados() {
        when(detalleConteoRepository.findByTomaInventarioIdTomaInventario(99L)).thenReturn(List.of());

        List<DetalleConteo> resultado = detalleConteoService.findByTomaInventario(99L);

        assertTrue(resultado.isEmpty());
        verify(detalleConteoRepository, times(1)).findByTomaInventarioIdTomaInventario(99L);
    }

    @Test
    void actualizarDetalleConteoExistente() {
        DetalleConteo datos = new DetalleConteo(null, 15, 12, -3, new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L), new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(detalleConteoRepository.findById(1L)).thenReturn(Optional.of(detalleConteo));
        when(tomaInventarioRepository.findById(1L)).thenReturn(Optional.of(new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L)));
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(detalleConteoRepository.save(any(DetalleConteo.class))).thenAnswer(inv -> inv.getArgument(0));

        DetalleConteo resultado = detalleConteoService.actualizarDetalleConteo(1L, datos);

        assertEquals(1L, resultado.getIdDetalleConteo());
        assertEquals(12, resultado.getCantidadContada());
        verify(detalleConteoRepository, times(1)).findById(1L);
        verify(detalleConteoRepository, times(1)).save(detalleConteo);
    }

    @Test
    void actualizarDetalleConteoNoExistente() {
        when(detalleConteoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> detalleConteoService.actualizarDetalleConteo(99L, detalleConteo));

        assertEquals("DetalleConteo no encontrado con id: 99", ex.getMessage());
        verify(detalleConteoRepository, times(1)).findById(99L);
        verify(detalleConteoRepository, never()).save(any(DetalleConteo.class));
    }

    @Test
    void eliminarDetalleConteo() {
        detalleConteoService.eliminarDetalleConteo(1L);

        verify(detalleConteoRepository, times(1)).deleteById(1L);
    }
}
