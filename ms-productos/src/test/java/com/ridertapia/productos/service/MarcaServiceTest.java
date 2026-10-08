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

import com.ridertapia.productos.model.Marca;

import com.ridertapia.productos.repository.MarcaRepository;

@ExtendWith(MockitoExtension.class)
class MarcaServiceTest {

    @Mock
    private MarcaRepository marcaRepository;

    @InjectMocks
    private MarcaService marcaService;

    private Marca marca;

    @BeforeEach
    void setUp() {
        marca = new Marca(1L, "Trek", true);
    }

    @Test
    void guardarMarca() {
        when(marcaRepository.save(marca)).thenReturn(marca);

        Marca resultado = marcaService.guardarMarca(marca);

        assertNotNull(resultado);
        assertEquals("Trek", resultado.getNombre());
        verify(marcaRepository, times(1)).save(marca);
    }

    @Test
    void listarMarcas() {
        Marca otro = new Marca(2L, "Specialized", true);
        when(marcaRepository.findAll()).thenReturn(Arrays.asList(marca, otro));

        List<Marca> resultado = marcaService.listarMarcas();

        assertEquals(2, resultado.size());
        verify(marcaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));

        Optional<Marca> resultado = marcaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Trek", resultado.get().getNombre());
        verify(marcaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(marcaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Marca> resultado = marcaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(marcaRepository, times(1)).findById(99L);
    }

    @Test
    void findByNombreExistente() {
        when(marcaRepository.findByNombre("Trek")).thenReturn(Optional.of(marca));

        Marca resultado = marcaService.findByNombre("Trek");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdMarca());
        verify(marcaRepository, times(1)).findByNombre("Trek");
    }

    @Test
    void findByNombreNoExistente() {
        when(marcaRepository.findByNombre("NO-EXISTE")).thenReturn(Optional.empty());

        Marca resultado = marcaService.findByNombre("NO-EXISTE");

        assertNull(resultado);
        verify(marcaRepository, times(1)).findByNombre("NO-EXISTE");
    }

    @Test
    void actualizarMarcaExistente() {
        Marca datos = new Marca(null, "Trek Bikes", false);
        when(marcaRepository.findById(1L)).thenReturn(Optional.of(marca));
        when(marcaRepository.save(any(Marca.class))).thenAnswer(inv -> inv.getArgument(0));

        Marca resultado = marcaService.actualizarMarca(1L, datos);

        assertEquals(1L, resultado.getIdMarca());
        assertEquals("Trek Bikes", resultado.getNombre());
        verify(marcaRepository, times(1)).findById(1L);
        verify(marcaRepository, times(1)).save(marca);
    }

    @Test
    void actualizarMarcaNoExistente() {
        when(marcaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> marcaService.actualizarMarca(99L, marca));

        assertEquals("Marca no encontrada con id: 99", ex.getMessage());
        verify(marcaRepository, times(1)).findById(99L);
        verify(marcaRepository, never()).save(any(Marca.class));
    }

    @Test
    void eliminarMarca() {
        marcaService.eliminarMarca(1L);

        verify(marcaRepository, times(1)).deleteById(1L);
    }
}
