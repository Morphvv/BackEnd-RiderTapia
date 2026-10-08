package com.ridertapia.pagos.service;

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

import java.time.LocalDate;
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

import com.ridertapia.pagos.model.CierreCaja;

import com.ridertapia.pagos.repository.CierreCajaRepository;

@ExtendWith(MockitoExtension.class)
class CierreCajaServiceTest {

    @Mock
    private CierreCajaRepository cierreCajaRepository;

    @InjectMocks
    private CierreCajaService cierreCajaService;

    private CierreCaja cierreCaja;

    @BeforeEach
    void setUp() {
        cierreCaja = new CierreCaja(1L, LocalDate.of(2026, 10, 6), 250000, 420000, 899990, 79990, 250000, 0, 1L);
    }

    @Test
    void guardarCierreCaja() {
        when(cierreCajaRepository.save(cierreCaja)).thenReturn(cierreCaja);

        CierreCaja resultado = cierreCajaService.guardarCierreCaja(cierreCaja);

        assertNotNull(resultado);
        assertEquals(250000, resultado.getEfectivoContado());
        verify(cierreCajaRepository, times(1)).save(cierreCaja);
    }

    @Test
    void listarCierresCaja() {
        CierreCaja otro = new CierreCaja(2L, LocalDate.of(2026, 10, 7), 180000, 310000, 0, 120000, 175000, -5000, 2L);
        when(cierreCajaRepository.findAll()).thenReturn(Arrays.asList(cierreCaja, otro));

        List<CierreCaja> resultado = cierreCajaService.listarCierresCaja();

        assertEquals(2, resultado.size());
        verify(cierreCajaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(cierreCajaRepository.findById(1L)).thenReturn(Optional.of(cierreCaja));

        Optional<CierreCaja> resultado = cierreCajaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(250000, resultado.get().getEfectivoContado());
        verify(cierreCajaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(cierreCajaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<CierreCaja> resultado = cierreCajaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(cierreCajaRepository, times(1)).findById(99L);
    }

    @Test
    void findByIdEmpleadoConResultados() {
        when(cierreCajaRepository.findByIdEmpleado(1L)).thenReturn(List.of(cierreCaja));

        List<CierreCaja> resultado = cierreCajaService.findByIdEmpleado(1L);

        assertEquals(1, resultado.size());
        verify(cierreCajaRepository, times(1)).findByIdEmpleado(1L);
    }

    @Test
    void findByIdEmpleadoSinResultados() {
        when(cierreCajaRepository.findByIdEmpleado(99L)).thenReturn(List.of());

        List<CierreCaja> resultado = cierreCajaService.findByIdEmpleado(99L);

        assertTrue(resultado.isEmpty());
        verify(cierreCajaRepository, times(1)).findByIdEmpleado(99L);
    }

    @Test
    void actualizarCierreCajaExistente() {
        CierreCaja datos = new CierreCaja(null, LocalDate.of(2026, 10, 8), 250000, 420000, 899990, 79990, 249000, -1000, 1L);
        when(cierreCajaRepository.findById(1L)).thenReturn(Optional.of(cierreCaja));
        when(cierreCajaRepository.save(any(CierreCaja.class))).thenAnswer(inv -> inv.getArgument(0));

        CierreCaja resultado = cierreCajaService.actualizarCierreCaja(1L, datos);

        assertEquals(1L, resultado.getIdCierreCaja());
        assertEquals(249000, resultado.getEfectivoContado());
        verify(cierreCajaRepository, times(1)).findById(1L);
        verify(cierreCajaRepository, times(1)).save(cierreCaja);
    }

    @Test
    void actualizarCierreCajaNoExistente() {
        when(cierreCajaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cierreCajaService.actualizarCierreCaja(99L, cierreCaja));

        assertEquals("CierreCaja no encontrado con id: 99", ex.getMessage());
        verify(cierreCajaRepository, times(1)).findById(99L);
        verify(cierreCajaRepository, never()).save(any(CierreCaja.class));
    }

    @Test
    void eliminarCierreCaja() {
        cierreCajaService.eliminarCierreCaja(1L);

        verify(cierreCajaRepository, times(1)).deleteById(1L);
    }
}
