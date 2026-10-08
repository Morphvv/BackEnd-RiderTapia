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

import com.ridertapia.inventario.model.AlertaStock;
import com.ridertapia.inventario.model.ItemInventario;

import com.ridertapia.inventario.repository.AlertaStockRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;

@ExtendWith(MockitoExtension.class)
class AlertaStockServiceTest {

    @Mock
    private AlertaStockRepository alertaStockRepository;

    @Mock
    private ItemInventarioRepository itemInventarioRepository;

    @InjectMocks
    private AlertaStockService alertaStockService;

    private AlertaStock alertaStock;

    @BeforeEach
    void setUp() {
        alertaStock = new AlertaStock(1L, LocalDateTime.of(2026, 10, 6, 9, 0), 2, false, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
    }

    @Test
    void guardarAlertaStock() {
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(alertaStockRepository.save(alertaStock)).thenReturn(alertaStock);

        AlertaStock resultado = alertaStockService.guardarAlertaStock(alertaStock);

        assertNotNull(resultado);
        assertEquals(2, resultado.getStockAlMomento());
        verify(alertaStockRepository, times(1)).save(alertaStock);
    }

    @Test
    void guardarAlertaStockConItemInventarioNoExistente() {
        alertaStock.setItemInventario(new ItemInventario(99L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(itemInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> alertaStockService.guardarAlertaStock(alertaStock));

        assertEquals("ItemInventario no encontrado con id: 99", ex.getMessage());
        verify(alertaStockRepository, never()).save(any(AlertaStock.class));
    }

    @Test
    void listarAlertasStock() {
        AlertaStock otro = new AlertaStock(2L, LocalDateTime.of(2026, 10, 6, 12, 0), 1, false, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(alertaStockRepository.findAll()).thenReturn(Arrays.asList(alertaStock, otro));

        List<AlertaStock> resultado = alertaStockService.listarAlertasStock();

        assertEquals(2, resultado.size());
        verify(alertaStockRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(alertaStockRepository.findById(1L)).thenReturn(Optional.of(alertaStock));

        Optional<AlertaStock> resultado = alertaStockService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(2, resultado.get().getStockAlMomento());
        verify(alertaStockRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(alertaStockRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<AlertaStock> resultado = alertaStockService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(alertaStockRepository, times(1)).findById(99L);
    }

    @Test
    void findByItemInventarioConResultados() {
        when(alertaStockRepository.findByItemInventarioIdItemInventario(1L)).thenReturn(List.of(alertaStock));

        List<AlertaStock> resultado = alertaStockService.findByItemInventario(1L);

        assertEquals(1, resultado.size());
        verify(alertaStockRepository, times(1)).findByItemInventarioIdItemInventario(1L);
    }

    @Test
    void findByItemInventarioSinResultados() {
        when(alertaStockRepository.findByItemInventarioIdItemInventario(99L)).thenReturn(List.of());

        List<AlertaStock> resultado = alertaStockService.findByItemInventario(99L);

        assertTrue(resultado.isEmpty());
        verify(alertaStockRepository, times(1)).findByItemInventarioIdItemInventario(99L);
    }

    @Test
    void actualizarAlertaStockExistente() {
        AlertaStock datos = new AlertaStock(null, LocalDateTime.of(2026, 10, 7, 9, 0), 0, true, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(alertaStockRepository.findById(1L)).thenReturn(Optional.of(alertaStock));
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(alertaStockRepository.save(any(AlertaStock.class))).thenAnswer(inv -> inv.getArgument(0));

        AlertaStock resultado = alertaStockService.actualizarAlertaStock(1L, datos);

        assertEquals(1L, resultado.getIdAlertaStock());
        assertEquals(0, resultado.getStockAlMomento());
        verify(alertaStockRepository, times(1)).findById(1L);
        verify(alertaStockRepository, times(1)).save(alertaStock);
    }

    @Test
    void actualizarAlertaStockNoExistente() {
        when(alertaStockRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> alertaStockService.actualizarAlertaStock(99L, alertaStock));

        assertEquals("AlertaStock no encontrada con id: 99", ex.getMessage());
        verify(alertaStockRepository, times(1)).findById(99L);
        verify(alertaStockRepository, never()).save(any(AlertaStock.class));
    }

    @Test
    void eliminarAlertaStock() {
        alertaStockService.eliminarAlertaStock(1L);

        verify(alertaStockRepository, times(1)).deleteById(1L);
    }
}
