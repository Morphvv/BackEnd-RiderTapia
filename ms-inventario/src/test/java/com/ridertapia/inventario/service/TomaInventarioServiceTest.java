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

import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.model.EstadoToma;
import com.ridertapia.inventario.repository.TomaInventarioRepository;

@ExtendWith(MockitoExtension.class)
class TomaInventarioServiceTest {

    @Mock
    private TomaInventarioRepository tomaInventarioRepository;

    @InjectMocks
    private TomaInventarioService tomaInventarioService;

    private TomaInventario tomaInventario;

    @BeforeEach
    void setUp() {
        tomaInventario = new TomaInventario(1L, LocalDateTime.of(2026, 10, 6, 9, 0), LocalDateTime.of(2026, 10, 6, 18, 0), EstadoToma.EN_CURSO, 1L);
    }

    @Test
    void guardarTomaInventario() {
        when(tomaInventarioRepository.save(tomaInventario)).thenReturn(tomaInventario);

        TomaInventario resultado = tomaInventarioService.guardarTomaInventario(tomaInventario);

        assertNotNull(resultado);
        assertEquals(EstadoToma.EN_CURSO, resultado.getEstado());
        verify(tomaInventarioRepository, times(1)).save(tomaInventario);
    }

    @Test
    void listarTomasInventario() {
        TomaInventario otro = new TomaInventario(2L, LocalDateTime.of(2026, 10, 6, 12, 0), LocalDateTime.of(2026, 10, 6, 19, 0), EstadoToma.EN_CURSO, 2L);
        when(tomaInventarioRepository.findAll()).thenReturn(Arrays.asList(tomaInventario, otro));

        List<TomaInventario> resultado = tomaInventarioService.listarTomasInventario();

        assertEquals(2, resultado.size());
        verify(tomaInventarioRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(tomaInventarioRepository.findById(1L)).thenReturn(Optional.of(tomaInventario));

        Optional<TomaInventario> resultado = tomaInventarioService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(EstadoToma.EN_CURSO, resultado.get().getEstado());
        verify(tomaInventarioRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(tomaInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<TomaInventario> resultado = tomaInventarioService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(tomaInventarioRepository, times(1)).findById(99L);
    }

    @Test
    void findByIdEmpleadoConResultados() {
        when(tomaInventarioRepository.findByIdEmpleado(1L)).thenReturn(List.of(tomaInventario));

        List<TomaInventario> resultado = tomaInventarioService.findByIdEmpleado(1L);

        assertEquals(1, resultado.size());
        verify(tomaInventarioRepository, times(1)).findByIdEmpleado(1L);
    }

    @Test
    void findByIdEmpleadoSinResultados() {
        when(tomaInventarioRepository.findByIdEmpleado(99L)).thenReturn(List.of());

        List<TomaInventario> resultado = tomaInventarioService.findByIdEmpleado(99L);

        assertTrue(resultado.isEmpty());
        verify(tomaInventarioRepository, times(1)).findByIdEmpleado(99L);
    }

    @Test
    void actualizarTomaInventarioExistente() {
        TomaInventario datos = new TomaInventario(null, LocalDateTime.of(2026, 10, 7, 9, 0), LocalDateTime.of(2026, 10, 7, 18, 0), EstadoToma.CONFIRMADA, 1L);
        when(tomaInventarioRepository.findById(1L)).thenReturn(Optional.of(tomaInventario));
        when(tomaInventarioRepository.save(any(TomaInventario.class))).thenAnswer(inv -> inv.getArgument(0));

        TomaInventario resultado = tomaInventarioService.actualizarTomaInventario(1L, datos);

        assertEquals(1L, resultado.getIdTomaInventario());
        assertEquals(EstadoToma.CONFIRMADA, resultado.getEstado());
        verify(tomaInventarioRepository, times(1)).findById(1L);
        verify(tomaInventarioRepository, times(1)).save(tomaInventario);
    }

    @Test
    void actualizarTomaInventarioNoExistente() {
        when(tomaInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> tomaInventarioService.actualizarTomaInventario(99L, tomaInventario));

        assertEquals("TomaInventario no encontrada con id: 99", ex.getMessage());
        verify(tomaInventarioRepository, times(1)).findById(99L);
        verify(tomaInventarioRepository, never()).save(any(TomaInventario.class));
    }

    @Test
    void eliminarTomaInventario() {
        tomaInventarioService.eliminarTomaInventario(1L);

        verify(tomaInventarioRepository, times(1)).deleteById(1L);
    }
}
