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

import com.ridertapia.pedidos.model.DatosVisitante;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.model.CanalVenta;
import com.ridertapia.pedidos.model.EstadoPedido;
import com.ridertapia.pedidos.repository.DatosVisitanteRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    private static final String CLIENTES_URL = "http://localhost:8082/api/v1/clientes";

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DatosVisitanteRepository datosVisitanteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pedidoService, "clientesUrl", CLIENTES_URL);
        pedido = new Pedido(1L, "PED-0001", LocalDateTime.of(2026, 10, 6, 10, 0), LocalDateTime.of(2026, 10, 7, 10, 0), EstadoPedido.PENDIENTE_PAGO, CanalVenta.WEB, 899990, 0, 899990, "No aplica", "12345678-5", 2L, 1L, null);
    }

    @Test
    void guardarPedido() {
        when(restTemplate.getForEntity(URI.create(CLIENTES_URL + "/12345678-5"), String.class)).thenReturn(ResponseEntity.ok("{}"));
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        Pedido resultado = pedidoService.guardarPedido(pedido);

        assertNotNull(resultado);
        assertEquals(899990, resultado.getTotal());
        verify(pedidoRepository, times(1)).save(pedido);
    }

    @Test
    void guardarPedidoConClienteNoExistente() {
        when(restTemplate.getForEntity(URI.create(CLIENTES_URL + "/12345678-5"), String.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoService.guardarPedido(pedido));

        assertEquals("Cliente no encontrado con rut: 12345678-5", ex.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void guardarPedidoDeVisitanteNoConsultaClientes() {
        pedido.setRutCliente(null);
        when(pedidoRepository.save(pedido)).thenReturn(pedido);

        pedidoService.guardarPedido(pedido);

        verify(restTemplate, never()).getForEntity(any(URI.class), any());
    }

    @Test
    void guardarPedidoConDatosVisitanteNoExistente() {
        pedido.setDatosVisitante(new DatosVisitante(99L, "Ana Soto", "ana@correo.cl", "+56911112222"));
        when(datosVisitanteRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoService.guardarPedido(pedido));

        assertEquals("DatosVisitante no encontrado con id: 99", ex.getMessage());
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void listarPedidos() {
        Pedido otro = new Pedido(2L, "PED-0002", LocalDateTime.of(2026, 10, 6, 12, 0), LocalDateTime.of(2026, 10, 7, 12, 0), EstadoPedido.RETIRADO, CanalVenta.TIENDA, 79990, 0, 79990, "No aplica", "11111111-1", 3L, 2L, null);
        when(pedidoRepository.findAll()).thenReturn(Arrays.asList(pedido, otro));

        List<Pedido> resultado = pedidoService.listarPedidos();

        assertEquals(2, resultado.size());
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Optional<Pedido> resultado = pedidoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(899990, resultado.get().getTotal());
        verify(pedidoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Pedido> resultado = pedidoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(pedidoRepository, times(1)).findById(99L);
    }

    @Test
    void findByNumeroExistente() {
        when(pedidoRepository.findByNumero("PED-0001")).thenReturn(Optional.of(pedido));

        Pedido resultado = pedidoService.findByNumero("PED-0001");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPedido());
        verify(pedidoRepository, times(1)).findByNumero("PED-0001");
    }

    @Test
    void findByNumeroNoExistente() {
        when(pedidoRepository.findByNumero("NO-EXISTE")).thenReturn(Optional.empty());

        Pedido resultado = pedidoService.findByNumero("NO-EXISTE");

        assertNull(resultado);
        verify(pedidoRepository, times(1)).findByNumero("NO-EXISTE");
    }

    @Test
    void actualizarPedidoExistente() {
        Pedido datos = new Pedido(null, "PED-0003", LocalDateTime.of(2026, 10, 7, 9, 0), LocalDateTime.of(2026, 10, 8, 9, 0), EstadoPedido.PAGADO, CanalVenta.WEB, 899990, 50000, 849990, "No aplica", "12345678-5", 2L, 1L, null);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido resultado = pedidoService.actualizarPedido(1L, datos);

        assertEquals(1L, resultado.getIdPedido());
        assertEquals(849990, resultado.getTotal());
        verify(pedidoRepository, times(1)).findById(1L);
        verify(pedidoRepository, times(1)).save(pedido);
    }

    @Test
    void actualizarPedidoNoExistente() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pedidoService.actualizarPedido(99L, pedido));

        assertEquals("Pedido no encontrado con id: 99", ex.getMessage());
        verify(pedidoRepository, times(1)).findById(99L);
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void eliminarPedido() {
        pedidoService.eliminarPedido(1L);

        verify(pedidoRepository, times(1)).deleteById(1L);
    }
}
