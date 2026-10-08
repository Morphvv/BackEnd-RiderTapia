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

import com.ridertapia.inventario.repository.CargaMasivaRepository;

@ExtendWith(MockitoExtension.class)
class CargaMasivaServiceTest {

    @Mock
    private CargaMasivaRepository cargaMasivaRepository;

    @InjectMocks
    private CargaMasivaService cargaMasivaService;

    private CargaMasiva cargaMasiva;

    @BeforeEach
    void setUp() {
        cargaMasiva = new CargaMasiva(1L, "productos_octubre.xlsx", LocalDateTime.of(2026, 10, 6, 9, 0), 120, 2, 1L);
    }

    @Test
    void guardarCargaMasiva() {
        when(cargaMasivaRepository.save(cargaMasiva)).thenReturn(cargaMasiva);

        CargaMasiva resultado = cargaMasivaService.guardarCargaMasiva(cargaMasiva);

        assertNotNull(resultado);
        assertEquals(120, resultado.getFilasProcesadas());
        verify(cargaMasivaRepository, times(1)).save(cargaMasiva);
    }

    @Test
    void listarCargasMasivas() {
        CargaMasiva otro = new CargaMasiva(2L, "repuestos.xlsx", LocalDateTime.of(2026, 10, 6, 12, 0), 40, 0, 2L);
        when(cargaMasivaRepository.findAll()).thenReturn(Arrays.asList(cargaMasiva, otro));

        List<CargaMasiva> resultado = cargaMasivaService.listarCargasMasivas();

        assertEquals(2, resultado.size());
        verify(cargaMasivaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(cargaMasivaRepository.findById(1L)).thenReturn(Optional.of(cargaMasiva));

        Optional<CargaMasiva> resultado = cargaMasivaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(120, resultado.get().getFilasProcesadas());
        verify(cargaMasivaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(cargaMasivaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<CargaMasiva> resultado = cargaMasivaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(cargaMasivaRepository, times(1)).findById(99L);
    }

    @Test
    void findByIdEmpleadoConResultados() {
        when(cargaMasivaRepository.findByIdEmpleado(1L)).thenReturn(List.of(cargaMasiva));

        List<CargaMasiva> resultado = cargaMasivaService.findByIdEmpleado(1L);

        assertEquals(1, resultado.size());
        verify(cargaMasivaRepository, times(1)).findByIdEmpleado(1L);
    }

    @Test
    void findByIdEmpleadoSinResultados() {
        when(cargaMasivaRepository.findByIdEmpleado(99L)).thenReturn(List.of());

        List<CargaMasiva> resultado = cargaMasivaService.findByIdEmpleado(99L);

        assertTrue(resultado.isEmpty());
        verify(cargaMasivaRepository, times(1)).findByIdEmpleado(99L);
    }

    @Test
    void actualizarCargaMasivaExistente() {
        CargaMasiva datos = new CargaMasiva(null, "productos_octubre_v2.xlsx", LocalDateTime.of(2026, 10, 7, 9, 0), 118, 1, 1L);
        when(cargaMasivaRepository.findById(1L)).thenReturn(Optional.of(cargaMasiva));
        when(cargaMasivaRepository.save(any(CargaMasiva.class))).thenAnswer(inv -> inv.getArgument(0));

        CargaMasiva resultado = cargaMasivaService.actualizarCargaMasiva(1L, datos);

        assertEquals(1L, resultado.getIdCargaMasiva());
        assertEquals(118, resultado.getFilasProcesadas());
        verify(cargaMasivaRepository, times(1)).findById(1L);
        verify(cargaMasivaRepository, times(1)).save(cargaMasiva);
    }

    @Test
    void actualizarCargaMasivaNoExistente() {
        when(cargaMasivaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cargaMasivaService.actualizarCargaMasiva(99L, cargaMasiva));

        assertEquals("CargaMasiva no encontrada con id: 99", ex.getMessage());
        verify(cargaMasivaRepository, times(1)).findById(99L);
        verify(cargaMasivaRepository, never()).save(any(CargaMasiva.class));
    }

    @Test
    void eliminarCargaMasiva() {
        cargaMasivaService.eliminarCargaMasiva(1L);

        verify(cargaMasivaRepository, times(1)).deleteById(1L);
    }
}
