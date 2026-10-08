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

import com.ridertapia.inventario.model.CargaMasiva;
import com.ridertapia.inventario.model.ErrorCargaMasiva;

import com.ridertapia.inventario.repository.CargaMasivaRepository;
import com.ridertapia.inventario.repository.ErrorCargaMasivaRepository;

@ExtendWith(MockitoExtension.class)
class ErrorCargaMasivaServiceTest {

    @Mock
    private ErrorCargaMasivaRepository errorCargaMasivaRepository;

    @Mock
    private CargaMasivaRepository cargaMasivaRepository;

    @InjectMocks
    private ErrorCargaMasivaService errorCargaMasivaService;

    private ErrorCargaMasiva errorCargaMasiva;

    @BeforeEach
    void setUp() {
        errorCargaMasiva = new ErrorCargaMasiva(1L, 15, "SKU repetido", new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L));
    }

    @Test
    void guardarErrorCargaMasiva() {
        when(cargaMasivaRepository.findById(1L)).thenReturn(Optional.of(new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L)));
        when(errorCargaMasivaRepository.save(errorCargaMasiva)).thenReturn(errorCargaMasiva);

        ErrorCargaMasiva resultado = errorCargaMasivaService.guardarErrorCargaMasiva(errorCargaMasiva);

        assertNotNull(resultado);
        assertEquals("SKU repetido", resultado.getMensaje());
        verify(errorCargaMasivaRepository, times(1)).save(errorCargaMasiva);
    }

    @Test
    void guardarErrorCargaMasivaConCargaMasivaNoExistente() {
        errorCargaMasiva.setCargaMasiva(new CargaMasiva(99L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L));
        when(cargaMasivaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> errorCargaMasivaService.guardarErrorCargaMasiva(errorCargaMasiva));

        assertEquals("CargaMasiva no encontrada con id: 99", ex.getMessage());
        verify(errorCargaMasivaRepository, never()).save(any(ErrorCargaMasiva.class));
    }

    @Test
    void listarErroresCargaMasiva() {
        ErrorCargaMasiva otro = new ErrorCargaMasiva(2L, 87, "Precio vacío", new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L));
        when(errorCargaMasivaRepository.findAll()).thenReturn(Arrays.asList(errorCargaMasiva, otro));

        List<ErrorCargaMasiva> resultado = errorCargaMasivaService.listarErroresCargaMasiva();

        assertEquals(2, resultado.size());
        verify(errorCargaMasivaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(errorCargaMasivaRepository.findById(1L)).thenReturn(Optional.of(errorCargaMasiva));

        Optional<ErrorCargaMasiva> resultado = errorCargaMasivaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("SKU repetido", resultado.get().getMensaje());
        verify(errorCargaMasivaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(errorCargaMasivaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ErrorCargaMasiva> resultado = errorCargaMasivaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(errorCargaMasivaRepository, times(1)).findById(99L);
    }

    @Test
    void findByCargaMasivaConResultados() {
        when(errorCargaMasivaRepository.findByCargaMasivaIdCargaMasiva(1L)).thenReturn(List.of(errorCargaMasiva));

        List<ErrorCargaMasiva> resultado = errorCargaMasivaService.findByCargaMasiva(1L);

        assertEquals(1, resultado.size());
        verify(errorCargaMasivaRepository, times(1)).findByCargaMasivaIdCargaMasiva(1L);
    }

    @Test
    void findByCargaMasivaSinResultados() {
        when(errorCargaMasivaRepository.findByCargaMasivaIdCargaMasiva(99L)).thenReturn(List.of());

        List<ErrorCargaMasiva> resultado = errorCargaMasivaService.findByCargaMasiva(99L);

        assertTrue(resultado.isEmpty());
        verify(errorCargaMasivaRepository, times(1)).findByCargaMasivaIdCargaMasiva(99L);
    }

    @Test
    void actualizarErrorCargaMasivaExistente() {
        ErrorCargaMasiva datos = new ErrorCargaMasiva(null, 16, "SKU no existe", new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L));
        when(errorCargaMasivaRepository.findById(1L)).thenReturn(Optional.of(errorCargaMasiva));
        when(cargaMasivaRepository.findById(1L)).thenReturn(Optional.of(new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L)));
        when(errorCargaMasivaRepository.save(any(ErrorCargaMasiva.class))).thenAnswer(inv -> inv.getArgument(0));

        ErrorCargaMasiva resultado = errorCargaMasivaService.actualizarErrorCargaMasiva(1L, datos);

        assertEquals(1L, resultado.getIdErrorCargaMasiva());
        assertEquals("SKU no existe", resultado.getMensaje());
        verify(errorCargaMasivaRepository, times(1)).findById(1L);
        verify(errorCargaMasivaRepository, times(1)).save(errorCargaMasiva);
    }

    @Test
    void actualizarErrorCargaMasivaNoExistente() {
        when(errorCargaMasivaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> errorCargaMasivaService.actualizarErrorCargaMasiva(99L, errorCargaMasiva));

        assertEquals("ErrorCargaMasiva no encontrado con id: 99", ex.getMessage());
        verify(errorCargaMasivaRepository, times(1)).findById(99L);
        verify(errorCargaMasivaRepository, never()).save(any(ErrorCargaMasiva.class));
    }

    @Test
    void eliminarErrorCargaMasiva() {
        errorCargaMasivaService.eliminarErrorCargaMasiva(1L);

        verify(errorCargaMasivaRepository, times(1)).deleteById(1L);
    }
}
