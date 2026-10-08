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

import com.ridertapia.inventario.model.AjusteInventario;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.model.EstadoToma;
import com.ridertapia.inventario.model.MotivoAjuste;
import com.ridertapia.inventario.model.TipoMovimiento;
import com.ridertapia.inventario.repository.AjusteInventarioRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;
import com.ridertapia.inventario.repository.TomaInventarioRepository;

@ExtendWith(MockitoExtension.class)
class AjusteInventarioServiceTest {

    @Mock
    private AjusteInventarioRepository ajusteInventarioRepository;

    @Mock
    private ItemInventarioRepository itemInventarioRepository;

    @Mock
    private TomaInventarioRepository tomaInventarioRepository;

    @InjectMocks
    private AjusteInventarioService ajusteInventarioService;

    private AjusteInventario ajusteInventario;

    @BeforeEach
    void setUp() {
        ajusteInventario = new AjusteInventario(1L, TipoMovimiento.INGRESO, 10, 5, 15, LocalDateTime.of(2026, 10, 6, 9, 0), "Recepción de factura", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), MotivoAjuste.MERMA, null);
    }

    @Test
    void guardarAjusteInventario() {
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(ajusteInventarioRepository.save(ajusteInventario)).thenReturn(ajusteInventario);

        AjusteInventario resultado = ajusteInventarioService.guardarAjusteInventario(ajusteInventario);

        assertNotNull(resultado);
        assertEquals(MotivoAjuste.MERMA, resultado.getMotivo());
        verify(ajusteInventarioRepository, times(1)).save(ajusteInventario);
    }

    @Test
    void guardarAjusteInventarioConItemInventarioNoExistente() {
        ajusteInventario.setItemInventario(new ItemInventario(99L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))));
        when(itemInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> ajusteInventarioService.guardarAjusteInventario(ajusteInventario));

        assertEquals("ItemInventario no encontrado con id: 99", ex.getMessage());
        verify(ajusteInventarioRepository, never()).save(any(AjusteInventario.class));
    }

    @Test
    void listarAjustesInventario() {
        AjusteInventario otro = new AjusteInventario(2L, TipoMovimiento.VENTA_WEB, 2, 15, 13, LocalDateTime.of(2026, 10, 6, 12, 0), "Venta pedido PED-0001", 2L, 6L, 4L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), MotivoAjuste.ERROR_CONTEO, null);
        when(ajusteInventarioRepository.findAll()).thenReturn(Arrays.asList(ajusteInventario, otro));

        List<AjusteInventario> resultado = ajusteInventarioService.listarAjustesInventario();

        assertEquals(2, resultado.size());
        verify(ajusteInventarioRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(ajusteInventarioRepository.findById(1L)).thenReturn(Optional.of(ajusteInventario));

        Optional<AjusteInventario> resultado = ajusteInventarioService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(MotivoAjuste.MERMA, resultado.get().getMotivo());
        verify(ajusteInventarioRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(ajusteInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<AjusteInventario> resultado = ajusteInventarioService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(ajusteInventarioRepository, times(1)).findById(99L);
    }

    @Test
    void findByItemInventarioConResultados() {
        when(ajusteInventarioRepository.findByItemInventarioIdItemInventario(1L)).thenReturn(List.of(ajusteInventario));

        List<AjusteInventario> resultado = ajusteInventarioService.findByItemInventario(1L);

        assertEquals(1, resultado.size());
        verify(ajusteInventarioRepository, times(1)).findByItemInventarioIdItemInventario(1L);
    }

    @Test
    void findByItemInventarioSinResultados() {
        when(ajusteInventarioRepository.findByItemInventarioIdItemInventario(99L)).thenReturn(List.of());

        List<AjusteInventario> resultado = ajusteInventarioService.findByItemInventario(99L);

        assertTrue(resultado.isEmpty());
        verify(ajusteInventarioRepository, times(1)).findByItemInventarioIdItemInventario(99L);
    }

    @Test
    void actualizarAjusteInventarioExistente() {
        AjusteInventario datos = new AjusteInventario(null, TipoMovimiento.AJUSTE, 3, 13, 10, LocalDateTime.of(2026, 10, 7, 9, 0), "Ajuste por conteo", 1L, 5L, 3L, new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L))), MotivoAjuste.CONTEO_FISICO, null);
        when(ajusteInventarioRepository.findById(1L)).thenReturn(Optional.of(ajusteInventario));
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)))));
        when(ajusteInventarioRepository.save(any(AjusteInventario.class))).thenAnswer(inv -> inv.getArgument(0));

        AjusteInventario resultado = ajusteInventarioService.actualizarAjusteInventario(1L, datos);

        assertEquals(1L, resultado.getIdMovimientoInventario());
        assertEquals(MotivoAjuste.CONTEO_FISICO, resultado.getMotivo());
        verify(ajusteInventarioRepository, times(1)).findById(1L);
        verify(ajusteInventarioRepository, times(1)).save(ajusteInventario);
    }

    @Test
    void actualizarAjusteInventarioNoExistente() {
        when(ajusteInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ajusteInventarioService.actualizarAjusteInventario(99L, ajusteInventario));

        assertEquals("AjusteInventario no encontrado con id: 99", ex.getMessage());
        verify(ajusteInventarioRepository, times(1)).findById(99L);
        verify(ajusteInventarioRepository, never()).save(any(AjusteInventario.class));
    }

    @Test
    void eliminarAjusteInventario() {
        ajusteInventarioService.eliminarAjusteInventario(1L);

        verify(ajusteInventarioRepository, times(1)).deleteById(1L);
    }
}
