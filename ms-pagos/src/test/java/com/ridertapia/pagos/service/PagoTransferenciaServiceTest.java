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

import com.ridertapia.pagos.model.PagoTransferencia;
import com.ridertapia.pagos.model.EstadoPago;
import com.ridertapia.pagos.repository.PagoTransferenciaRepository;

@ExtendWith(MockitoExtension.class)
class PagoTransferenciaServiceTest {

    @Mock
    private PagoTransferenciaRepository pagoTransferenciaRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PagoTransferenciaService pagoTransferenciaService;

    private static final String PEDIDOS_URL = "http://localhost:8086/api/v1/pedidos";

    private PagoTransferencia pagoTransferencia;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagoTransferenciaService, "pedidoUrl", PEDIDOS_URL);
        pagoTransferencia = new PagoTransferencia(1L, 5L, 899990, LocalDateTime.of(2026, 10, 6, 10, 35), EstadoPago.APROBADO, "https://docs.ridertapia.cl/comp-1.pdf", LocalDateTime.of(2026, 10, 7, 23, 59), "Comprobante legible");
    }

    @Test
    void guardarPagoTransferencia() {
        when(restTemplate.getForEntity(URI.create(PEDIDOS_URL + "/5"), String.class))
                .thenReturn(ResponseEntity.ok("{}"));
        when(pagoTransferenciaRepository.save(pagoTransferencia)).thenReturn(pagoTransferencia);

        PagoTransferencia resultado = pagoTransferenciaService.guardarPagoTransferencia(pagoTransferencia);

        assertNotNull(resultado);
        assertEquals("Comprobante legible", resultado.getObservacion());
        verify(pagoTransferenciaRepository, times(1)).save(pagoTransferencia);
    }

    @Test
    void guardarPagoTransferenciaConPedidoNoExistente() {
        when(restTemplate.getForEntity(URI.create(PEDIDOS_URL + "/5"), String.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pagoTransferenciaService.guardarPagoTransferencia(pagoTransferencia));

        assertEquals("Pedido no encontrado con id: 5", ex.getMessage());
        verify(pagoTransferenciaRepository, never()).save(any(PagoTransferencia.class));
    }

    @Test
    void listarPagosTransferencia() {
        PagoTransferencia otro = new PagoTransferencia(2L, 6L, 79990, LocalDateTime.of(2026, 10, 6, 16, 0), EstadoPago.PENDIENTE, "https://docs.ridertapia.cl/comp-2.pdf", LocalDateTime.of(2026, 10, 8, 23, 59), "Pendiente de revisión");
        when(pagoTransferenciaRepository.findAll()).thenReturn(Arrays.asList(pagoTransferencia, otro));

        List<PagoTransferencia> resultado = pagoTransferenciaService.listarPagosTransferencia();

        assertEquals(2, resultado.size());
        verify(pagoTransferenciaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(pagoTransferenciaRepository.findById(1L)).thenReturn(Optional.of(pagoTransferencia));

        Optional<PagoTransferencia> resultado = pagoTransferenciaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Comprobante legible", resultado.get().getObservacion());
        verify(pagoTransferenciaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(pagoTransferenciaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PagoTransferencia> resultado = pagoTransferenciaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(pagoTransferenciaRepository, times(1)).findById(99L);
    }

    @Test
    void findByIdPedidoConResultados() {
        when(pagoTransferenciaRepository.findByIdPedido(5L)).thenReturn(List.of(pagoTransferencia));

        List<PagoTransferencia> resultado = pagoTransferenciaService.findByIdPedido(5L);

        assertEquals(1, resultado.size());
        verify(pagoTransferenciaRepository, times(1)).findByIdPedido(5L);
    }

    @Test
    void findByIdPedidoSinResultados() {
        when(pagoTransferenciaRepository.findByIdPedido(99L)).thenReturn(List.of());

        List<PagoTransferencia> resultado = pagoTransferenciaService.findByIdPedido(99L);

        assertTrue(resultado.isEmpty());
        verify(pagoTransferenciaRepository, times(1)).findByIdPedido(99L);
    }

    @Test
    void actualizarPagoTransferenciaExistente() {
        PagoTransferencia datos = new PagoTransferencia(null, 5L, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), EstadoPago.REEMBOLSADO, "https://docs.ridertapia.cl/comp-3.pdf", LocalDateTime.of(2026, 10, 9, 23, 59), "Monto no coincide");
        when(pagoTransferenciaRepository.findById(1L)).thenReturn(Optional.of(pagoTransferencia));
        when(pagoTransferenciaRepository.save(any(PagoTransferencia.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoTransferencia resultado = pagoTransferenciaService.actualizarPagoTransferencia(1L, datos);

        assertEquals(1L, resultado.getIdPago());
        assertEquals("Monto no coincide", resultado.getObservacion());
        verify(pagoTransferenciaRepository, times(1)).findById(1L);
        verify(pagoTransferenciaRepository, times(1)).save(pagoTransferencia);
    }

    @Test
    void actualizarPagoTransferenciaNoExistente() {
        when(pagoTransferenciaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pagoTransferenciaService.actualizarPagoTransferencia(99L, pagoTransferencia));

        assertEquals("PagoTransferencia no encontrado con id: 99", ex.getMessage());
        verify(pagoTransferenciaRepository, times(1)).findById(99L);
        verify(pagoTransferenciaRepository, never()).save(any(PagoTransferencia.class));
    }

    @Test
    void eliminarPagoTransferencia() {
        pagoTransferenciaService.eliminarPagoTransferencia(1L);

        verify(pagoTransferenciaRepository, times(1)).deleteById(1L);
    }
}
