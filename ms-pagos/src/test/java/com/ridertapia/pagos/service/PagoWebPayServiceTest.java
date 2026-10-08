package com.ridertapia.pagos.service;

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

import java.net.URI;
import java.nio.charset.StandardCharsets;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.pagos.model.PagoWebPay;
import com.ridertapia.pagos.model.EstadoPago;
import com.ridertapia.pagos.repository.PagoWebPayRepository;

@ExtendWith(MockitoExtension.class)
class PagoWebPayServiceTest {

    @Mock
    private PagoWebPayRepository pagoWebPayRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PagoWebPayService pagoWebPayService;

    private static final String PEDIDOS_URL = "http://localhost:8086/api/v1/pedidos";

    private PagoWebPay pagoWebPay;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagoWebPayService, "pedidoUrl", PEDIDOS_URL);
        pagoWebPay = new PagoWebPay(1L, 5L, 899990, LocalDateTime.of(2026, 10, 6, 10, 35), EstadoPago.APROBADO, "01ab7c9e2f", "AUT-123456", "4417");
    }

    @Test
    void guardarPagoWebPay() {
        when(restTemplate.getForEntity(URI.create(PEDIDOS_URL + "/5"), String.class))
                .thenReturn(ResponseEntity.ok("{}"));
        when(pagoWebPayRepository.save(pagoWebPay)).thenReturn(pagoWebPay);

        PagoWebPay resultado = pagoWebPayService.guardarPagoWebPay(pagoWebPay);

        assertNotNull(resultado);
        assertEquals("AUT-123456", resultado.getCodigoAutorizacion());
        verify(pagoWebPayRepository, times(1)).save(pagoWebPay);
    }

    @Test
    void guardarPagoWebPayConPedidoNoExistente() {
        when(restTemplate.getForEntity(URI.create(PEDIDOS_URL + "/5"), String.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pagoWebPayService.guardarPagoWebPay(pagoWebPay));

        assertEquals("Pedido no encontrado con id: 5", ex.getMessage());
        verify(pagoWebPayRepository, never()).save(any(PagoWebPay.class));
    }

    @Test
    void listarPagosWebPay() {
        PagoWebPay otro = new PagoWebPay(2L, 6L, 79990, LocalDateTime.of(2026, 10, 6, 16, 0), EstadoPago.PENDIENTE, "02cd8d0f3a", "AUT-654321", "1234");
        when(pagoWebPayRepository.findAll()).thenReturn(Arrays.asList(pagoWebPay, otro));

        List<PagoWebPay> resultado = pagoWebPayService.listarPagosWebPay();

        assertEquals(2, resultado.size());
        verify(pagoWebPayRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(pagoWebPayRepository.findById(1L)).thenReturn(Optional.of(pagoWebPay));

        Optional<PagoWebPay> resultado = pagoWebPayService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("AUT-123456", resultado.get().getCodigoAutorizacion());
        verify(pagoWebPayRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(pagoWebPayRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PagoWebPay> resultado = pagoWebPayService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(pagoWebPayRepository, times(1)).findById(99L);
    }

    @Test
    void findByIdPedidoConResultados() {
        when(pagoWebPayRepository.findByIdPedido(5L)).thenReturn(List.of(pagoWebPay));

        List<PagoWebPay> resultado = pagoWebPayService.findByIdPedido(5L);

        assertEquals(1, resultado.size());
        verify(pagoWebPayRepository, times(1)).findByIdPedido(5L);
    }

    @Test
    void findByIdPedidoSinResultados() {
        when(pagoWebPayRepository.findByIdPedido(99L)).thenReturn(List.of());

        List<PagoWebPay> resultado = pagoWebPayService.findByIdPedido(99L);

        assertTrue(resultado.isEmpty());
        verify(pagoWebPayRepository, times(1)).findByIdPedido(99L);
    }

    @Test
    void actualizarPagoWebPayExistente() {
        PagoWebPay datos = new PagoWebPay(null, 5L, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), EstadoPago.REEMBOLSADO, "03ef9e1a4b", "AUT-777777", "4417");
        when(pagoWebPayRepository.findById(1L)).thenReturn(Optional.of(pagoWebPay));
        when(pagoWebPayRepository.save(any(PagoWebPay.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoWebPay resultado = pagoWebPayService.actualizarPagoWebPay(1L, datos);

        assertEquals(1L, resultado.getIdPago());
        assertEquals("AUT-777777", resultado.getCodigoAutorizacion());
        verify(pagoWebPayRepository, times(1)).findById(1L);
        verify(pagoWebPayRepository, times(1)).save(pagoWebPay);
    }

    @Test
    void actualizarPagoWebPayNoExistente() {
        when(pagoWebPayRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pagoWebPayService.actualizarPagoWebPay(99L, pagoWebPay));

        assertEquals("PagoWebPay no encontrado con id: 99", ex.getMessage());
        verify(pagoWebPayRepository, times(1)).findById(99L);
        verify(pagoWebPayRepository, never()).save(any(PagoWebPay.class));
    }

    @Test
    void eliminarPagoWebPay() {
        pagoWebPayService.eliminarPagoWebPay(1L);

        verify(pagoWebPayRepository, times(1)).deleteById(1L);
    }
}
