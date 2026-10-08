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

import com.ridertapia.pedidos.model.DetallePedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.DetallePedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class DetallePedidoServiceTest {

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private DetallePedidoService detallePedidoService;

    private DetallePedido detallePedido;

    @BeforeEach
    void setUp() {
        detallePedido = new DetallePedido(1L, 10L, 1, 899990, 899990, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    @Test
    void guardarDetallePedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(detallePedidoRepository.save(detallePedido)).thenReturn(detallePedido);

        DetallePedido resultado = detallePedidoService.guardarDetallePedido(detallePedido);

        assertNotNull(resultado);
        assertEquals(1, resultado.getCantidad());
        verify(detallePedidoRepository, times(1)).save(detallePedido);
    }

    @Test
    void guardarDetallePedidoConPedidoNoExistente() {
        detallePedido.setPedido(new Pedido(99L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> detallePedidoService.guardarDetallePedido(detallePedido));

        assertEquals("Pedido no encontrado con id: 99", ex.getMessage());
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
    }

    @Test
    void listarDetallesPedido() {
        DetallePedido otro = new DetallePedido(2L, 11L, 2, 39995, 79990, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(detallePedidoRepository.findAll()).thenReturn(Arrays.asList(detallePedido, otro));

        List<DetallePedido> resultado = detallePedidoService.listarDetallesPedido();

        assertEquals(2, resultado.size());
        verify(detallePedidoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(detallePedidoRepository.findById(1L)).thenReturn(Optional.of(detallePedido));

        Optional<DetallePedido> resultado = detallePedidoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getCantidad());
        verify(detallePedidoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(detallePedidoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<DetallePedido> resultado = detallePedidoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(detallePedidoRepository, times(1)).findById(99L);
    }

    @Test
    void findByPedidoConResultados() {
        when(detallePedidoRepository.findByPedidoIdPedido(1L)).thenReturn(List.of(detallePedido));

        List<DetallePedido> resultado = detallePedidoService.findByPedido(1L);

        assertEquals(1, resultado.size());
        verify(detallePedidoRepository, times(1)).findByPedidoIdPedido(1L);
    }

    @Test
    void findByPedidoSinResultados() {
        when(detallePedidoRepository.findByPedidoIdPedido(99L)).thenReturn(List.of());

        List<DetallePedido> resultado = detallePedidoService.findByPedido(99L);

        assertTrue(resultado.isEmpty());
        verify(detallePedidoRepository, times(1)).findByPedidoIdPedido(99L);
    }

    @Test
    void actualizarDetallePedidoExistente() {
        DetallePedido datos = new DetallePedido(null, 10L, 3, 899990, 2699970, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(detallePedidoRepository.findById(1L)).thenReturn(Optional.of(detallePedido));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(detallePedidoRepository.save(any(DetallePedido.class))).thenAnswer(inv -> inv.getArgument(0));

        DetallePedido resultado = detallePedidoService.actualizarDetallePedido(1L, datos);

        assertEquals(1L, resultado.getIdDetallePedido());
        assertEquals(3, resultado.getCantidad());
        verify(detallePedidoRepository, times(1)).findById(1L);
        verify(detallePedidoRepository, times(1)).save(detallePedido);
    }

    @Test
    void actualizarDetallePedidoNoExistente() {
        when(detallePedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> detallePedidoService.actualizarDetallePedido(99L, detallePedido));

        assertEquals("DetallePedido no encontrado con id: 99", ex.getMessage());
        verify(detallePedidoRepository, times(1)).findById(99L);
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
    }

    @Test
    void eliminarDetallePedido() {
        detallePedidoService.eliminarDetallePedido(1L);

        verify(detallePedidoRepository, times(1)).deleteById(1L);
    }
}
