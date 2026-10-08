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

import java.time.LocalDate;
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

import com.ridertapia.pedidos.model.Factura;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.FacturaRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @Mock
    private FacturaRepository facturaRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private FacturaService facturaService;

    private Factura factura;

    @BeforeEach
    void setUp() {
        factura = new Factura(1L, 1001, LocalDate.of(2026, 10, 6), 756294, 143696, 899990, "https://docs.ridertapia.cl/1001.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null), "76123456-7", "Ciclismo Sur SpA", "Venta de artículos deportivos", "Barros Arana 123, Concepción");
    }

    @Test
    void guardarFactura() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(facturaRepository.save(factura)).thenReturn(factura);

        Factura resultado = facturaService.guardarFactura(factura);

        assertNotNull(resultado);
        assertEquals("Ciclismo Sur SpA", resultado.getRazonSocial());
        verify(facturaRepository, times(1)).save(factura);
    }

    @Test
    void guardarFacturaConPedidoNoExistente() {
        factura.setPedido(new Pedido(99L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> facturaService.guardarFactura(factura));

        assertEquals("Pedido no encontrado con id: 99", ex.getMessage());
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    void listarFacturas() {
        Factura otro = new Factura(2L, 1002, LocalDate.of(2026, 10, 7), 67218, 12772, 79990, "https://docs.ridertapia.cl/1002.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null), "77987654-3", "Ruedas del Biobío Ltda", "Taller de bicicletas", "O'Higgins 456, Concepción");
        when(facturaRepository.findAll()).thenReturn(Arrays.asList(factura, otro));

        List<Factura> resultado = facturaService.listarFacturas();

        assertEquals(2, resultado.size());
        verify(facturaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));

        Optional<Factura> resultado = facturaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Ciclismo Sur SpA", resultado.get().getRazonSocial());
        verify(facturaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Factura> resultado = facturaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(facturaRepository, times(1)).findById(99L);
    }

    @Test
    void findByPedidoConResultados() {
        when(facturaRepository.findByPedidoIdPedido(1L)).thenReturn(List.of(factura));

        List<Factura> resultado = facturaService.findByPedido(1L);

        assertEquals(1, resultado.size());
        verify(facturaRepository, times(1)).findByPedidoIdPedido(1L);
    }

    @Test
    void findByPedidoSinResultados() {
        when(facturaRepository.findByPedidoIdPedido(99L)).thenReturn(List.of());

        List<Factura> resultado = facturaService.findByPedido(99L);

        assertTrue(resultado.isEmpty());
        verify(facturaRepository, times(1)).findByPedidoIdPedido(99L);
    }

    @Test
    void actualizarFacturaExistente() {
        Factura datos = new Factura(null, 1003, LocalDate.of(2026, 10, 8), 922681, 175309, 1097990, "https://docs.ridertapia.cl/1003.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null), "76123456-7", "Ciclismo Sur Limitada", "Comercio", "Colo Colo 789, Concepción");
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(factura));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));

        Factura resultado = facturaService.actualizarFactura(1L, datos);

        assertEquals(1L, resultado.getIdDocumentoTributario());
        assertEquals("Ciclismo Sur Limitada", resultado.getRazonSocial());
        verify(facturaRepository, times(1)).findById(1L);
        verify(facturaRepository, times(1)).save(factura);
    }

    @Test
    void actualizarFacturaNoExistente() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> facturaService.actualizarFactura(99L, factura));

        assertEquals("Factura no encontrada con id: 99", ex.getMessage());
        verify(facturaRepository, times(1)).findById(99L);
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    void eliminarFactura() {
        facturaService.eliminarFactura(1L);

        verify(facturaRepository, times(1)).deleteById(1L);
    }
}
