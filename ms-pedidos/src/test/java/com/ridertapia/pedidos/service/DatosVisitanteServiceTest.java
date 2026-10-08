package com.ridertapia.pedidos.service;

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

import com.ridertapia.pedidos.model.DatosVisitante;

import com.ridertapia.pedidos.repository.DatosVisitanteRepository;

@ExtendWith(MockitoExtension.class)
class DatosVisitanteServiceTest {

    @Mock
    private DatosVisitanteRepository datosVisitanteRepository;

    @InjectMocks
    private DatosVisitanteService datosVisitanteService;

    private DatosVisitante datosVisitante;

    @BeforeEach
    void setUp() {
        datosVisitante = new DatosVisitante(1L, "Ana Soto", "ana@correo.cl", "+56911112222");
    }

    @Test
    void guardarDatosVisitante() {
        when(datosVisitanteRepository.save(datosVisitante)).thenReturn(datosVisitante);

        DatosVisitante resultado = datosVisitanteService.guardarDatosVisitante(datosVisitante);

        assertNotNull(resultado);
        assertEquals("Ana Soto", resultado.getNombre());
        verify(datosVisitanteRepository, times(1)).save(datosVisitante);
    }

    @Test
    void listarDatosVisitantes() {
        DatosVisitante otro = new DatosVisitante(2L, "Luis Rojas", "luis@correo.cl", "+56933334444");
        when(datosVisitanteRepository.findAll()).thenReturn(Arrays.asList(datosVisitante, otro));

        List<DatosVisitante> resultado = datosVisitanteService.listarDatosVisitantes();

        assertEquals(2, resultado.size());
        verify(datosVisitanteRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(datosVisitanteRepository.findById(1L)).thenReturn(Optional.of(datosVisitante));

        Optional<DatosVisitante> resultado = datosVisitanteService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Ana Soto", resultado.get().getNombre());
        verify(datosVisitanteRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(datosVisitanteRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<DatosVisitante> resultado = datosVisitanteService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(datosVisitanteRepository, times(1)).findById(99L);
    }

    @Test
    void findByCorreoConResultados() {
        when(datosVisitanteRepository.findByCorreo("ana@correo.cl")).thenReturn(List.of(datosVisitante));

        List<DatosVisitante> resultado = datosVisitanteService.findByCorreo("ana@correo.cl");

        assertEquals(1, resultado.size());
        verify(datosVisitanteRepository, times(1)).findByCorreo("ana@correo.cl");
    }

    @Test
    void findByCorreoSinResultados() {
        when(datosVisitanteRepository.findByCorreo("BMX")).thenReturn(List.of());

        List<DatosVisitante> resultado = datosVisitanteService.findByCorreo("BMX");

        assertTrue(resultado.isEmpty());
        verify(datosVisitanteRepository, times(1)).findByCorreo("BMX");
    }

    @Test
    void actualizarDatosVisitanteExistente() {
        DatosVisitante datos = new DatosVisitante(null, "Ana Soto Pérez", "ana.soto@correo.cl", "+56955556666");
        when(datosVisitanteRepository.findById(1L)).thenReturn(Optional.of(datosVisitante));
        when(datosVisitanteRepository.save(any(DatosVisitante.class))).thenAnswer(inv -> inv.getArgument(0));

        DatosVisitante resultado = datosVisitanteService.actualizarDatosVisitante(1L, datos);

        assertEquals(1L, resultado.getIdDatosVisitante());
        assertEquals("Ana Soto Pérez", resultado.getNombre());
        verify(datosVisitanteRepository, times(1)).findById(1L);
        verify(datosVisitanteRepository, times(1)).save(datosVisitante);
    }

    @Test
    void actualizarDatosVisitanteNoExistente() {
        when(datosVisitanteRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> datosVisitanteService.actualizarDatosVisitante(99L, datosVisitante));

        assertEquals("DatosVisitante no encontrado con id: 99", ex.getMessage());
        verify(datosVisitanteRepository, times(1)).findById(99L);
        verify(datosVisitanteRepository, never()).save(any(DatosVisitante.class));
    }

    @Test
    void eliminarDatosVisitante() {
        datosVisitanteService.eliminarDatosVisitante(1L);

        verify(datosVisitanteRepository, times(1)).deleteById(1L);
    }
}
