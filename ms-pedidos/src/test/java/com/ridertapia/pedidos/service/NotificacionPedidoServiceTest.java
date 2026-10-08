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

import com.ridertapia.pedidos.model.NotificacionPedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.NotificacionPedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class NotificacionPedidoServiceTest {

    @Mock
    private NotificacionPedidoRepository notificacionPedidoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private NotificacionPedidoService notificacionPedidoService;

    private NotificacionPedido notificacionPedido;

    @BeforeEach
    void setUp() {
        notificacionPedido = new NotificacionPedido(1L, "CONFIRMACION_COMPRA", "ana@correo.cl", LocalDateTime.of(2026, 10, 6, 10, 0), true, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    @Test
    void guardarNotificacionPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(notificacionPedidoRepository.save(notificacionPedido)).thenReturn(notificacionPedido);

        NotificacionPedido resultado = notificacionPedidoService.guardarNotificacionPedido(notificacionPedido);

        assertNotNull(resultado);
        assertEquals("CONFIRMACION_COMPRA", resultado.getTipo());
        verify(notificacionPedidoRepository, times(1)).save(notificacionPedido);
    }

    @Test
    void guardarNotificacionPedidoConPedidoNoExistente() {
        notificacionPedido.setPedido(new Pedido(99L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> notificacionPedidoService.guardarNotificacionPedido(notificacionPedido));

        assertEquals("Pedido no encontrado con id: 99", ex.getMessage());
        verify(notificacionPedidoRepository, never()).save(any(NotificacionPedido.class));
    }

    @Test
    void listarNotificacionesPedido() {
        NotificacionPedido otro = new NotificacionPedido(2L, "LISTO_PARA_RETIRO", "ana@correo.cl", LocalDateTime.of(2026, 10, 6, 12, 0), false, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(notificacionPedidoRepository.findAll()).thenReturn(Arrays.asList(notificacionPedido, otro));

        List<NotificacionPedido> resultado = notificacionPedidoService.listarNotificacionesPedido();

        assertEquals(2, resultado.size());
        verify(notificacionPedidoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(notificacionPedidoRepository.findById(1L)).thenReturn(Optional.of(notificacionPedido));

        Optional<NotificacionPedido> resultado = notificacionPedidoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("CONFIRMACION_COMPRA", resultado.get().getTipo());
        verify(notificacionPedidoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(notificacionPedidoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<NotificacionPedido> resultado = notificacionPedidoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(notificacionPedidoRepository, times(1)).findById(99L);
    }

    @Test
    void findByPedidoConResultados() {
        when(notificacionPedidoRepository.findByPedidoIdPedido(1L)).thenReturn(List.of(notificacionPedido));

        List<NotificacionPedido> resultado = notificacionPedidoService.findByPedido(1L);

        assertEquals(1, resultado.size());
        verify(notificacionPedidoRepository, times(1)).findByPedidoIdPedido(1L);
    }

    @Test
    void findByPedidoSinResultados() {
        when(notificacionPedidoRepository.findByPedidoIdPedido(99L)).thenReturn(List.of());

        List<NotificacionPedido> resultado = notificacionPedidoService.findByPedido(99L);

        assertTrue(resultado.isEmpty());
        verify(notificacionPedidoRepository, times(1)).findByPedidoIdPedido(99L);
    }

    @Test
    void actualizarNotificacionPedidoExistente() {
        NotificacionPedido datos = new NotificacionPedido(null, "PEDIDO_RETIRADO", "ana@correo.cl", LocalDateTime.of(2026, 10, 7, 9, 0), true, new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(notificacionPedidoRepository.findById(1L)).thenReturn(Optional.of(notificacionPedido));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(notificacionPedidoRepository.save(any(NotificacionPedido.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificacionPedido resultado = notificacionPedidoService.actualizarNotificacionPedido(1L, datos);

        assertEquals(1L, resultado.getIdNotificacionPedido());
        assertEquals("PEDIDO_RETIRADO", resultado.getTipo());
        verify(notificacionPedidoRepository, times(1)).findById(1L);
        verify(notificacionPedidoRepository, times(1)).save(notificacionPedido);
    }

    @Test
    void actualizarNotificacionPedidoNoExistente() {
        when(notificacionPedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> notificacionPedidoService.actualizarNotificacionPedido(99L, notificacionPedido));

        assertEquals("NotificacionPedido no encontrada con id: 99", ex.getMessage());
        verify(notificacionPedidoRepository, times(1)).findById(99L);
        verify(notificacionPedidoRepository, never()).save(any(NotificacionPedido.class));
    }

    @Test
    void eliminarNotificacionPedido() {
        notificacionPedidoService.eliminarNotificacionPedido(1L);

        verify(notificacionPedidoRepository, times(1)).deleteById(1L);
    }
}
