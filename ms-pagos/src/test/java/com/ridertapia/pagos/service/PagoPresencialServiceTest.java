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

import com.ridertapia.pagos.model.PagoPresencial;
import com.ridertapia.pagos.model.EstadoPago;
import com.ridertapia.pagos.model.MedioPago;
import com.ridertapia.pagos.repository.PagoPresencialRepository;

@ExtendWith(MockitoExtension.class)
class PagoPresencialServiceTest {

    @Mock
    private PagoPresencialRepository pagoPresencialRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PagoPresencialService pagoPresencialService;

    private static final String PEDIDOS_URL = "http://localhost:8086/api/v1/pedidos";

    private PagoPresencial pagoPresencial;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagoPresencialService, "pedidoUrl", PEDIDOS_URL);
        pagoPresencial = new PagoPresencial(1L, 5L, 899990, LocalDateTime.of(2026, 10, 6, 10, 35), EstadoPago.APROBADO, MedioPago.EFECTIVO);
    }

    @Test
    void guardarPagoPresencial() {
        when(restTemplate.getForEntity(URI.create(PEDIDOS_URL + "/5"), String.class))
                .thenReturn(ResponseEntity.ok("{}"));
        when(pagoPresencialRepository.save(pagoPresencial)).thenReturn(pagoPresencial);

        PagoPresencial resultado = pagoPresencialService.guardarPagoPresencial(pagoPresencial);

        assertNotNull(resultado);
        assertEquals(MedioPago.EFECTIVO, resultado.getMedioPago());
        verify(pagoPresencialRepository, times(1)).save(pagoPresencial);
    }

    @Test
    void guardarPagoPresencialConPedidoNoExistente() {
        when(restTemplate.getForEntity(URI.create(PEDIDOS_URL + "/5"), String.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pagoPresencialService.guardarPagoPresencial(pagoPresencial));

        assertEquals("Pedido no encontrado con id: 5", ex.getMessage());
        verify(pagoPresencialRepository, never()).save(any(PagoPresencial.class));
    }

    @Test
    void listarPagosPresenciales() {
        PagoPresencial otro = new PagoPresencial(2L, 6L, 79990, LocalDateTime.of(2026, 10, 6, 16, 0), EstadoPago.PENDIENTE, MedioPago.DEBITO);
        when(pagoPresencialRepository.findAll()).thenReturn(Arrays.asList(pagoPresencial, otro));

        List<PagoPresencial> resultado = pagoPresencialService.listarPagosPresenciales();

        assertEquals(2, resultado.size());
        verify(pagoPresencialRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(pagoPresencialRepository.findById(1L)).thenReturn(Optional.of(pagoPresencial));

        Optional<PagoPresencial> resultado = pagoPresencialService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(MedioPago.EFECTIVO, resultado.get().getMedioPago());
        verify(pagoPresencialRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(pagoPresencialRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<PagoPresencial> resultado = pagoPresencialService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(pagoPresencialRepository, times(1)).findById(99L);
    }

    @Test
    void findByIdPedidoConResultados() {
        when(pagoPresencialRepository.findByIdPedido(5L)).thenReturn(List.of(pagoPresencial));

        List<PagoPresencial> resultado = pagoPresencialService.findByIdPedido(5L);

        assertEquals(1, resultado.size());
        verify(pagoPresencialRepository, times(1)).findByIdPedido(5L);
    }

    @Test
    void findByIdPedidoSinResultados() {
        when(pagoPresencialRepository.findByIdPedido(99L)).thenReturn(List.of());

        List<PagoPresencial> resultado = pagoPresencialService.findByIdPedido(99L);

        assertTrue(resultado.isEmpty());
        verify(pagoPresencialRepository, times(1)).findByIdPedido(99L);
    }

    @Test
    void actualizarPagoPresencialExistente() {
        PagoPresencial datos = new PagoPresencial(null, 5L, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), EstadoPago.REEMBOLSADO, MedioPago.CREDITO);
        when(pagoPresencialRepository.findById(1L)).thenReturn(Optional.of(pagoPresencial));
        when(pagoPresencialRepository.save(any(PagoPresencial.class))).thenAnswer(inv -> inv.getArgument(0));

        PagoPresencial resultado = pagoPresencialService.actualizarPagoPresencial(1L, datos);

        assertEquals(1L, resultado.getIdPago());
        assertEquals(MedioPago.CREDITO, resultado.getMedioPago());
        verify(pagoPresencialRepository, times(1)).findById(1L);
        verify(pagoPresencialRepository, times(1)).save(pagoPresencial);
    }

    @Test
    void actualizarPagoPresencialNoExistente() {
        when(pagoPresencialRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pagoPresencialService.actualizarPagoPresencial(99L, pagoPresencial));

        assertEquals("PagoPresencial no encontrado con id: 99", ex.getMessage());
        verify(pagoPresencialRepository, times(1)).findById(99L);
        verify(pagoPresencialRepository, never()).save(any(PagoPresencial.class));
    }

    @Test
    void eliminarPagoPresencial() {
        pagoPresencialService.eliminarPagoPresencial(1L);

        verify(pagoPresencialRepository, times(1)).deleteById(1L);
    }
}
