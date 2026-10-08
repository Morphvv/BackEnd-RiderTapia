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

import com.ridertapia.inventario.model.IngresoMercancia;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TipoMovimiento;
import com.ridertapia.inventario.repository.IngresoMercanciaRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;

@ExtendWith(MockitoExtension.class)
class IngresoMercanciaServiceTest {

    @Mock
    private IngresoMercanciaRepository ingresoMercanciaRepository;

    @Mock
    private ItemInventarioRepository itemInventarioRepository;

    @InjectMocks
    private IngresoMercanciaService ingresoMercanciaService;

    private IngresoMercancia ingresoMercancia;

    @BeforeEach
    void setUp() {
        ingresoMercancia = new IngresoMercancia(1L, TipoMovimiento.INGRESO, 10, 5, 15, LocalDateTime.of(2026, 10, 6, 9, 0), "Recepción de factura", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), 640000, "FAC-5501", 1L);
    }

    @Test
    void guardarIngresoMercancia() {
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(ingresoMercanciaRepository.save(ingresoMercancia)).thenReturn(ingresoMercancia);

        IngresoMercancia resultado = ingresoMercanciaService.guardarIngresoMercancia(ingresoMercancia);

        assertNotNull(resultado);
        assertEquals("FAC-5501", resultado.getNumeroDocumento());
        verify(ingresoMercanciaRepository, times(1)).save(ingresoMercancia);
    }

    @Test
    void guardarIngresoMercanciaConItemInventarioNoExistente() {
        ingresoMercancia.setItemInventario(new ItemInventario(99L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(itemInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> ingresoMercanciaService.guardarIngresoMercancia(ingresoMercancia));

        assertEquals("ItemInventario no encontrado con id: 99", ex.getMessage());
        verify(ingresoMercanciaRepository, never()).save(any(IngresoMercancia.class));
    }

    @Test
    void listarIngresosMercancia() {
        IngresoMercancia otro = new IngresoMercancia(2L, TipoMovimiento.VENTA_WEB, 2, 15, 13, LocalDateTime.of(2026, 10, 6, 12, 0), "Venta pedido PED-0001", 2L, 6L, 4L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), 650000, "FAC-5502", 2L);
        when(ingresoMercanciaRepository.findAll()).thenReturn(Arrays.asList(ingresoMercancia, otro));

        List<IngresoMercancia> resultado = ingresoMercanciaService.listarIngresosMercancia();

        assertEquals(2, resultado.size());
        verify(ingresoMercanciaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(ingresoMercanciaRepository.findById(1L)).thenReturn(Optional.of(ingresoMercancia));

        Optional<IngresoMercancia> resultado = ingresoMercanciaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("FAC-5501", resultado.get().getNumeroDocumento());
        verify(ingresoMercanciaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(ingresoMercanciaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<IngresoMercancia> resultado = ingresoMercanciaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(ingresoMercanciaRepository, times(1)).findById(99L);
    }

    @Test
    void findByItemInventarioConResultados() {
        when(ingresoMercanciaRepository.findByItemInventarioIdItemInventario(1L)).thenReturn(List.of(ingresoMercancia));

        List<IngresoMercancia> resultado = ingresoMercanciaService.findByItemInventario(1L);

        assertEquals(1, resultado.size());
        verify(ingresoMercanciaRepository, times(1)).findByItemInventarioIdItemInventario(1L);
    }

    @Test
    void findByItemInventarioSinResultados() {
        when(ingresoMercanciaRepository.findByItemInventarioIdItemInventario(99L)).thenReturn(List.of());

        List<IngresoMercancia> resultado = ingresoMercanciaService.findByItemInventario(99L);

        assertTrue(resultado.isEmpty());
        verify(ingresoMercanciaRepository, times(1)).findByItemInventarioIdItemInventario(99L);
    }

    @Test
    void actualizarIngresoMercanciaExistente() {
        IngresoMercancia datos = new IngresoMercancia(null, TipoMovimiento.AJUSTE, 3, 13, 10, LocalDateTime.of(2026, 10, 7, 9, 0), "Ajuste por conteo", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), 630000, "FAC-5503", 1L);
        when(ingresoMercanciaRepository.findById(1L)).thenReturn(Optional.of(ingresoMercancia));
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(ingresoMercanciaRepository.save(any(IngresoMercancia.class))).thenAnswer(inv -> inv.getArgument(0));

        IngresoMercancia resultado = ingresoMercanciaService.actualizarIngresoMercancia(1L, datos);

        assertEquals(1L, resultado.getIdMovimientoInventario());
        assertEquals("FAC-5503", resultado.getNumeroDocumento());
        verify(ingresoMercanciaRepository, times(1)).findById(1L);
        verify(ingresoMercanciaRepository, times(1)).save(ingresoMercancia);
    }

    @Test
    void actualizarIngresoMercanciaNoExistente() {
        when(ingresoMercanciaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ingresoMercanciaService.actualizarIngresoMercancia(99L, ingresoMercancia));

        assertEquals("IngresoMercancia no encontrado con id: 99", ex.getMessage());
        verify(ingresoMercanciaRepository, times(1)).findById(99L);
        verify(ingresoMercanciaRepository, never()).save(any(IngresoMercancia.class));
    }

    @Test
    void eliminarIngresoMercancia() {
        ingresoMercanciaService.eliminarIngresoMercancia(1L);

        verify(ingresoMercanciaRepository, times(1)).deleteById(1L);
    }
}
