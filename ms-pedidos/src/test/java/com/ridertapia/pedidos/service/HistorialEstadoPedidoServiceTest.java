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

import com.ridertapia.pedidos.model.HistorialEstadoPedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.HistorialEstadoPedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class HistorialEstadoPedidoServiceTest {

    @Mock
    private HistorialEstadoPedidoRepository historialEstadoPedidoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private HistorialEstadoPedidoService historialEstadoPedidoService;

    private HistorialEstadoPedido historialEstadoPedido;

    @BeforeEach
    void setUp() {
        historialEstadoPedido = new HistorialEstadoPedido(1L, EstadoPedido.PENDIENTE_PAGO, EstadoPedido.PAGADO, LocalDateTime.of(2026, 10, 6, 10, 0), 1L, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    @Test
    void guardarHistorialEstadoPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(historialEstadoPedidoRepository.save(historialEstadoPedido)).thenReturn(historialEstadoPedido);

        HistorialEstadoPedido resultado = historialEstadoPedidoService.guardarHistorialEstadoPedido(historialEstadoPedido);

        assertNotNull(resultado);
        assertEquals(EstadoPedido.PAGADO, resultado.getEstadoNuevo());
        verify(historialEstadoPedidoRepository, times(1)).save(historialEstadoPedido);
    }

    @Test
    void guardarHistorialEstadoPedidoConPedidoNoExistente() {
        historialEstadoPedido.setPedido(new Pedido(99L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> historialEstadoPedidoService.guardarHistorialEstadoPedido(historialEstadoPedido));

        assertEquals("Pedido no encontrado con id: 99", ex.getMessage());
        verify(historialEstadoPedidoRepository, never()).save(any(HistorialEstadoPedido.class));
    }

    @Test
    void listarHistorialesEstadoPedido() {
        HistorialEstadoPedido otro = new HistorialEstadoPedido(2L, EstadoPedido.PAGADO, EstadoPedido.EN_PREPARACION, LocalDateTime.of(2026, 10, 6, 12, 0), 2L, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(historialEstadoPedidoRepository.findAll()).thenReturn(Arrays.asList(historialEstadoPedido, otro));

        List<HistorialEstadoPedido> resultado = historialEstadoPedidoService.listarHistorialesEstadoPedido();

        assertEquals(2, resultado.size());
        verify(historialEstadoPedidoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(historialEstadoPedidoRepository.findById(1L)).thenReturn(Optional.of(historialEstadoPedido));

        Optional<HistorialEstadoPedido> resultado = historialEstadoPedidoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(EstadoPedido.PAGADO, resultado.get().getEstadoNuevo());
        verify(historialEstadoPedidoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(historialEstadoPedidoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<HistorialEstadoPedido> resultado = historialEstadoPedidoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(historialEstadoPedidoRepository, times(1)).findById(99L);
    }

    @Test
    void findByPedidoConResultados() {
        when(historialEstadoPedidoRepository.findByPedidoIdPedido(1L)).thenReturn(List.of(historialEstadoPedido));

        List<HistorialEstadoPedido> resultado = historialEstadoPedidoService.findByPedido(1L);

        assertEquals(1, resultado.size());
        verify(historialEstadoPedidoRepository, times(1)).findByPedidoIdPedido(1L);
    }

    @Test
    void findByPedidoSinResultados() {
        when(historialEstadoPedidoRepository.findByPedidoIdPedido(99L)).thenReturn(List.of());

        List<HistorialEstadoPedido> resultado = historialEstadoPedidoService.findByPedido(99L);

        assertTrue(resultado.isEmpty());
        verify(historialEstadoPedidoRepository, times(1)).findByPedidoIdPedido(99L);
    }

    @Test
    void actualizarHistorialEstadoPedidoExistente() {
        HistorialEstadoPedido datos = new HistorialEstadoPedido(null, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO_PARA_RETIRO, LocalDateTime.of(2026, 10, 7, 9, 0), 3L, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(historialEstadoPedidoRepository.findById(1L)).thenReturn(Optional.of(historialEstadoPedido));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(historialEstadoPedidoRepository.save(any(HistorialEstadoPedido.class))).thenAnswer(inv -> inv.getArgument(0));

        HistorialEstadoPedido resultado = historialEstadoPedidoService.actualizarHistorialEstadoPedido(1L, datos);

        assertEquals(1L, resultado.getIdHistorialEstadoPedido());
        assertEquals(EstadoPedido.LISTO_PARA_RETIRO, resultado.getEstadoNuevo());
        verify(historialEstadoPedidoRepository, times(1)).findById(1L);
        verify(historialEstadoPedidoRepository, times(1)).save(historialEstadoPedido);
    }

    @Test
    void actualizarHistorialEstadoPedidoNoExistente() {
        when(historialEstadoPedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> historialEstadoPedidoService.actualizarHistorialEstadoPedido(99L, historialEstadoPedido));

        assertEquals("HistorialEstadoPedido no encontrado con id: 99", ex.getMessage());
        verify(historialEstadoPedidoRepository, times(1)).findById(99L);
        verify(historialEstadoPedidoRepository, never()).save(any(HistorialEstadoPedido.class));
    }

    @Test
    void eliminarHistorialEstadoPedido() {
        historialEstadoPedidoService.eliminarHistorialEstadoPedido(1L);

        verify(historialEstadoPedidoRepository, times(1)).deleteById(1L);
    }
}
