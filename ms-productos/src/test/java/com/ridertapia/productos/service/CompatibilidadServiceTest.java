package com.ridertapia.productos.service;

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

import com.ridertapia.productos.model.Compatibilidad;

import com.ridertapia.productos.repository.CompatibilidadRepository;

@ExtendWith(MockitoExtension.class)
class CompatibilidadServiceTest {

    @Mock
    private CompatibilidadRepository compatibilidadRepository;

    @InjectMocks
    private CompatibilidadService compatibilidadService;

    private Compatibilidad compatibilidad;

    @BeforeEach
    void setUp() {
        compatibilidad = new Compatibilidad(1L, "MTB", "29", "12");
    }

    @Test
    void guardarCompatibilidad() {
        when(compatibilidadRepository.save(compatibilidad)).thenReturn(compatibilidad);

        Compatibilidad resultado = compatibilidadService.guardarCompatibilidad(compatibilidad);

        assertNotNull(resultado);
        assertEquals("29", resultado.getAro());
        verify(compatibilidadRepository, times(1)).save(compatibilidad);
    }

    @Test
    void listarCompatibilidades() {
        Compatibilidad otro = new Compatibilidad(2L, "Ruta", "700c", "11");
        when(compatibilidadRepository.findAll()).thenReturn(Arrays.asList(compatibilidad, otro));

        List<Compatibilidad> resultado = compatibilidadService.listarCompatibilidades();

        assertEquals(2, resultado.size());
        verify(compatibilidadRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(compatibilidadRepository.findById(1L)).thenReturn(Optional.of(compatibilidad));

        Optional<Compatibilidad> resultado = compatibilidadService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("29", resultado.get().getAro());
        verify(compatibilidadRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(compatibilidadRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Compatibilidad> resultado = compatibilidadService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(compatibilidadRepository, times(1)).findById(99L);
    }

    @Test
    void findByTipoBicicletaConResultados() {
        when(compatibilidadRepository.findByTipoBicicleta("MTB")).thenReturn(List.of(compatibilidad));

        List<Compatibilidad> resultado = compatibilidadService.findByTipoBicicleta("MTB");

        assertEquals(1, resultado.size());
        verify(compatibilidadRepository, times(1)).findByTipoBicicleta("MTB");
    }

    @Test
    void findByTipoBicicletaSinResultados() {
        when(compatibilidadRepository.findByTipoBicicleta("BMX")).thenReturn(List.of());

        List<Compatibilidad> resultado = compatibilidadService.findByTipoBicicleta("BMX");

        assertTrue(resultado.isEmpty());
        verify(compatibilidadRepository, times(1)).findByTipoBicicleta("BMX");
    }

    @Test
    void actualizarCompatibilidadExistente() {
        Compatibilidad datos = new Compatibilidad(null, "MTB", "27.5", "10");
        when(compatibilidadRepository.findById(1L)).thenReturn(Optional.of(compatibilidad));
        when(compatibilidadRepository.save(any(Compatibilidad.class))).thenAnswer(inv -> inv.getArgument(0));

        Compatibilidad resultado = compatibilidadService.actualizarCompatibilidad(1L, datos);

        assertEquals(1L, resultado.getIdCompatibilidad());
        assertEquals("27.5", resultado.getAro());
        verify(compatibilidadRepository, times(1)).findById(1L);
        verify(compatibilidadRepository, times(1)).save(compatibilidad);
    }

    @Test
    void actualizarCompatibilidadNoExistente() {
        when(compatibilidadRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> compatibilidadService.actualizarCompatibilidad(99L, compatibilidad));

        assertEquals("Compatibilidad no encontrada con id: 99", ex.getMessage());
        verify(compatibilidadRepository, times(1)).findById(99L);
        verify(compatibilidadRepository, never()).save(any(Compatibilidad.class));
    }

    @Test
    void eliminarCompatibilidad() {
        compatibilidadService.eliminarCompatibilidad(1L);

        verify(compatibilidadRepository, times(1)).deleteById(1L);
    }
}
