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

import com.ridertapia.pedidos.model.Boleta;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.BoletaRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock
    private BoletaRepository boletaRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private BoletaService boletaService;

    private Boleta boleta;

    @BeforeEach
    void setUp() {
        boleta = new Boleta(1L, 1001, LocalDate.of(2026, 10, 6), 756294, 143696, 899990, "https://docs.ridertapia.cl/1001.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
    }

    @Test
    void guardarBoleta() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(boletaRepository.save(boleta)).thenReturn(boleta);

        Boleta resultado = boletaService.guardarBoleta(boleta);

        assertNotNull(resultado);
        assertEquals(1001, resultado.getFolio());
        verify(boletaRepository, times(1)).save(boleta);
    }

    @Test
    void guardarBoletaConPedidoNoExistente() {
        boleta.setPedido(new Pedido(99L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> boletaService.guardarBoleta(boleta));

        assertEquals("Pedido no encontrado con id: 99", ex.getMessage());
        verify(boletaRepository, never()).save(any(Boleta.class));
    }

    @Test
    void listarBoletas() {
        Boleta otro = new Boleta(2L, 1002, LocalDate.of(2026, 10, 7), 67218, 12772, 79990, "https://docs.ridertapia.cl/1002.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(boletaRepository.findAll()).thenReturn(Arrays.asList(boleta, otro));

        List<Boleta> resultado = boletaService.listarBoletas();

        assertEquals(2, resultado.size());
        verify(boletaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));

        Optional<Boleta> resultado = boletaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1001, resultado.get().getFolio());
        verify(boletaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(boletaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Boleta> resultado = boletaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(boletaRepository, times(1)).findById(99L);
    }

    @Test
    void findByPedidoConResultados() {
        when(boletaRepository.findByPedidoIdPedido(1L)).thenReturn(List.of(boleta));

        List<Boleta> resultado = boletaService.findByPedido(1L);

        assertEquals(1, resultado.size());
        verify(boletaRepository, times(1)).findByPedidoIdPedido(1L);
    }

    @Test
    void findByPedidoSinResultados() {
        when(boletaRepository.findByPedidoIdPedido(99L)).thenReturn(List.of());

        List<Boleta> resultado = boletaService.findByPedido(99L);

        assertTrue(resultado.isEmpty());
        verify(boletaRepository, times(1)).findByPedidoIdPedido(99L);
    }

    @Test
    void actualizarBoletaExistente() {
        Boleta datos = new Boleta(null, 1003, LocalDate.of(2026, 10, 8), 922681, 175309, 1097990, "https://docs.ridertapia.cl/1003.pdf", new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null));
        when(boletaRepository.findById(1L)).thenReturn(Optional.of(boleta));
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null)));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(inv -> inv.getArgument(0));

        Boleta resultado = boletaService.actualizarBoleta(1L, datos);

        assertEquals(1L, resultado.getIdDocumentoTributario());
        assertEquals(1003, resultado.getFolio());
        verify(boletaRepository, times(1)).findById(1L);
        verify(boletaRepository, times(1)).save(boleta);
    }

    @Test
    void actualizarBoletaNoExistente() {
        when(boletaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> boletaService.actualizarBoleta(99L, boleta));

        assertEquals("Boleta no encontrada con id: 99", ex.getMessage());
        verify(boletaRepository, times(1)).findById(99L);
        verify(boletaRepository, never()).save(any(Boleta.class));
    }

    @Test
    void eliminarBoleta() {
        boletaService.eliminarBoleta(1L);

        verify(boletaRepository, times(1)).deleteById(1L);
    }
}
