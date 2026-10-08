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

import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.MovimientoInventario;
import com.ridertapia.inventario.model.TipoMovimiento;
import com.ridertapia.inventario.repository.ItemInventarioRepository;
import com.ridertapia.inventario.repository.MovimientoInventarioRepository;

@ExtendWith(MockitoExtension.class)
class MovimientoInventarioServiceTest {

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Mock
    private ItemInventarioRepository itemInventarioRepository;

    @InjectMocks
    private MovimientoInventarioService movimientoInventarioService;

    private MovimientoInventario movimientoInventario;

    @BeforeEach
    void setUp() {
        movimientoInventario = new MovimientoInventario(1L, TipoMovimiento.INGRESO, 10, 5, 15, LocalDateTime.of(2026, 10, 6, 9, 0), "Recepción de factura", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    @Test
    void guardarMovimientoInventario() {
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(movimientoInventarioRepository.save(movimientoInventario)).thenReturn(movimientoInventario);

        MovimientoInventario resultado = movimientoInventarioService.guardarMovimientoInventario(movimientoInventario);

        assertNotNull(resultado);
        assertEquals(10, resultado.getCantidad());
        verify(movimientoInventarioRepository, times(1)).save(movimientoInventario);
    }

    @Test
    void guardarMovimientoInventarioConItemInventarioNoExistente() {
        movimientoInventario.setItemInventario(new ItemInventario(99L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(itemInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> movimientoInventarioService.guardarMovimientoInventario(movimientoInventario));

        assertEquals("ItemInventario no encontrado con id: 99", ex.getMessage());
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void listarMovimientosInventario() {
        MovimientoInventario otro = new MovimientoInventario(2L, TipoMovimiento.VENTA_WEB, 2, 15, 13, LocalDateTime.of(2026, 10, 6, 12, 0), "Venta pedido PED-0001", 2L, 6L, 4L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(movimientoInventarioRepository.findAll()).thenReturn(Arrays.asList(movimientoInventario, otro));

        List<MovimientoInventario> resultado = movimientoInventarioService.listarMovimientosInventario();

        assertEquals(2, resultado.size());
        verify(movimientoInventarioRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(movimientoInventarioRepository.findById(1L)).thenReturn(Optional.of(movimientoInventario));

        Optional<MovimientoInventario> resultado = movimientoInventarioService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(10, resultado.get().getCantidad());
        verify(movimientoInventarioRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(movimientoInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<MovimientoInventario> resultado = movimientoInventarioService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(movimientoInventarioRepository, times(1)).findById(99L);
    }

    @Test
    void findByItemInventarioConResultados() {
        when(movimientoInventarioRepository.findByItemInventarioIdItemInventario(1L)).thenReturn(List.of(movimientoInventario));

        List<MovimientoInventario> resultado = movimientoInventarioService.findByItemInventario(1L);

        assertEquals(1, resultado.size());
        verify(movimientoInventarioRepository, times(1)).findByItemInventarioIdItemInventario(1L);
    }

    @Test
    void findByItemInventarioSinResultados() {
        when(movimientoInventarioRepository.findByItemInventarioIdItemInventario(99L)).thenReturn(List.of());

        List<MovimientoInventario> resultado = movimientoInventarioService.findByItemInventario(99L);

        assertTrue(resultado.isEmpty());
        verify(movimientoInventarioRepository, times(1)).findByItemInventarioIdItemInventario(99L);
    }

    @Test
    void actualizarMovimientoInventarioExistente() {
        MovimientoInventario datos = new MovimientoInventario(null, TipoMovimiento.AJUSTE, 3, 13, 10, LocalDateTime.of(2026, 10, 7, 9, 0), "Ajuste por conteo", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(movimientoInventarioRepository.findById(1L)).thenReturn(Optional.of(movimientoInventario));
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(movimientoInventarioRepository.save(any(MovimientoInventario.class))).thenAnswer(inv -> inv.getArgument(0));

        MovimientoInventario resultado = movimientoInventarioService.actualizarMovimientoInventario(1L, datos);

        assertEquals(1L, resultado.getIdMovimientoInventario());
        assertEquals(3, resultado.getCantidad());
        verify(movimientoInventarioRepository, times(1)).findById(1L);
        verify(movimientoInventarioRepository, times(1)).save(movimientoInventario);
    }

    @Test
    void actualizarMovimientoInventarioNoExistente() {
        when(movimientoInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> movimientoInventarioService.actualizarMovimientoInventario(99L, movimientoInventario));

        assertEquals("MovimientoInventario no encontrado con id: 99", ex.getMessage());
        verify(movimientoInventarioRepository, times(1)).findById(99L);
        verify(movimientoInventarioRepository, never()).save(any(MovimientoInventario.class));
    }

    @Test
    void eliminarMovimientoInventario() {
        movimientoInventarioService.eliminarMovimientoInventario(1L);

        verify(movimientoInventarioRepository, times(1)).deleteById(1L);
    }
}
