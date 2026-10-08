package com.ridertapia.carrito.service;

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

import com.ridertapia.carrito.model.Carrito;
import com.ridertapia.carrito.model.EstadoCarrito;
import com.ridertapia.carrito.repository.CarritoRepository;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    private static final String CLIENTES_URL = "http://localhost:8082/api/v1/clientes";

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CarritoService carritoService;

    private Carrito carrito;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(carritoService, "clientesUrl", CLIENTES_URL);
        carrito = new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L);
    }

    @Test
    void guardarCarrito() {
        when(restTemplate.getForEntity(URI.create(CLIENTES_URL + "/12345678-5"), String.class)).thenReturn(ResponseEntity.ok("{}"));
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        Carrito resultado = carritoService.guardarCarrito(carrito);

        assertNotNull(resultado);
        assertEquals(899990, resultado.getTotal());
        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    void guardarCarritoConClienteNoExistente() {
        when(restTemplate.getForEntity(URI.create(CLIENTES_URL + "/12345678-5"), String.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> carritoService.guardarCarrito(carrito));

        assertEquals("Cliente no encontrado con rut: 12345678-5", ex.getMessage());
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    void guardarCarritoAnonimoNoConsultaClientes() {
        carrito.setRutCliente(null);
        when(carritoRepository.save(carrito)).thenReturn(carrito);

        carritoService.guardarCarrito(carrito);

        verify(restTemplate, never()).getForEntity(any(URI.class), any());
    }

    @Test
    void listarCarritos() {
        Carrito otro = new Carrito(2L, "11111111-1", "tok-def456", LocalDateTime.of(2026, 10, 6, 12, 0), LocalDateTime.of(2026, 10, 6, 12, 30), EstadoCarrito.ACTIVO, 79990, 0, 79990, 3L);
        when(carritoRepository.findAll()).thenReturn(Arrays.asList(carrito, otro));

        List<Carrito> resultado = carritoService.listarCarritos();

        assertEquals(2, resultado.size());
        verify(carritoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        Optional<Carrito> resultado = carritoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(899990, resultado.get().getTotal());
        verify(carritoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Carrito> resultado = carritoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(carritoRepository, times(1)).findById(99L);
    }

    @Test
    void findByRutClienteConResultados() {
        when(carritoRepository.findByRutCliente("12345678-5")).thenReturn(List.of(carrito));

        List<Carrito> resultado = carritoService.findByRutCliente("12345678-5");

        assertEquals(1, resultado.size());
        verify(carritoRepository, times(1)).findByRutCliente("12345678-5");
    }

    @Test
    void findByRutClienteSinResultados() {
        when(carritoRepository.findByRutCliente("99999999-9")).thenReturn(List.of());

        List<Carrito> resultado = carritoService.findByRutCliente("99999999-9");

        assertTrue(resultado.isEmpty());
        verify(carritoRepository, times(1)).findByRutCliente("99999999-9");
    }

    @Test
    void actualizarCarritoExistente() {
        Carrito datos = new Carrito(null, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 7, 9, 0), LocalDateTime.of(2026, 10, 7, 9, 30), EstadoCarrito.CONVERTIDO_EN_PEDIDO, 899990, 89999, 809991, 2L);
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito resultado = carritoService.actualizarCarrito(1L, datos);

        assertEquals(1L, resultado.getIdCarrito());
        assertEquals(809991, resultado.getTotal());
        verify(carritoRepository, times(1)).findById(1L);
        verify(carritoRepository, times(1)).save(carrito);
    }

    @Test
    void actualizarCarritoNoExistente() {
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> carritoService.actualizarCarrito(99L, carrito));

        assertEquals("Carrito no encontrado con id: 99", ex.getMessage());
        verify(carritoRepository, times(1)).findById(99L);
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    void eliminarCarrito() {
        carritoService.eliminarCarrito(1L);

        verify(carritoRepository, times(1)).deleteById(1L);
    }
}
