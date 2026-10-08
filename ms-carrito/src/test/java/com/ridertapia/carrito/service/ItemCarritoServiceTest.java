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
import java.util.Map;
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
import com.ridertapia.carrito.model.ItemCarrito;
import com.ridertapia.carrito.model.EstadoCarrito;
import com.ridertapia.carrito.repository.CarritoRepository;
import com.ridertapia.carrito.repository.ItemCarritoRepository;

@ExtendWith(MockitoExtension.class)
class ItemCarritoServiceTest {

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ItemCarritoService itemCarritoService;

    private static final String VARIANTES_URL = "http://localhost:8085/api/v1/variantes";

    private static final String INVENTARIO_URL = "http://localhost:8088/api/v1/items-inventario";

    private static final URI URI_STOCK = URI.create(INVENTARIO_URL + "/idVariante/10");

    private ItemCarrito itemCarrito;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(itemCarritoService, "varianteUrl", VARIANTES_URL);
        ReflectionTestUtils.setField(itemCarritoService, "inventarioUrl", INVENTARIO_URL);
        itemCarrito = new ItemCarrito(1L, 10L, 1, 899990, LocalDateTime.of(2026, 10, 6, 11, 0), new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L));
    }

    @Test
    void guardarItemCarrito() {
        when(restTemplate.getForEntity(URI.create(VARIANTES_URL + "/10"), String.class))
                .thenReturn(ResponseEntity.ok("{}"));
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L)));
        when(restTemplate.getForObject(URI_STOCK, Map.class)).thenReturn(Map.of("stockDisponible", 5));
        when(itemCarritoRepository.save(itemCarrito)).thenReturn(itemCarrito);

        ItemCarrito resultado = itemCarritoService.guardarItemCarrito(itemCarrito);

        assertNotNull(resultado);
        assertEquals(1, resultado.getCantidad());
        verify(itemCarritoRepository, times(1)).save(itemCarrito);
    }

    @Test
    void guardarItemCarritoConCarritoNoExistente() {
        itemCarrito.setCarrito(new Carrito(99L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L));
        when(restTemplate.getForEntity(URI.create(VARIANTES_URL + "/10"), String.class))
                .thenReturn(ResponseEntity.ok("{}"));
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> itemCarritoService.guardarItemCarrito(itemCarrito));

        assertEquals("Carrito no encontrado con id: 99", ex.getMessage());
        verify(itemCarritoRepository, never()).save(any(ItemCarrito.class));
    }

    @Test
    void guardarItemCarritoConVarianteNoExistente() {
        when(restTemplate.getForEntity(URI.create(VARIANTES_URL + "/10"), String.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> itemCarritoService.guardarItemCarrito(itemCarrito));

        assertEquals("Variante no encontrada con id: 10", ex.getMessage());
        verify(itemCarritoRepository, never()).save(any(ItemCarrito.class));
    }

    @Test
    void listarItemsCarrito() {
        ItemCarrito otro = new ItemCarrito(2L, 11L, 2, 39995, LocalDateTime.of(2026, 10, 6, 12, 0), new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L));
        when(itemCarritoRepository.findAll()).thenReturn(Arrays.asList(itemCarrito, otro));

        List<ItemCarrito> resultado = itemCarritoService.listarItemsCarrito();

        assertEquals(2, resultado.size());
        verify(itemCarritoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(itemCarrito));

        Optional<ItemCarrito> resultado = itemCarritoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1, resultado.get().getCantidad());
        verify(itemCarritoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ItemCarrito> resultado = itemCarritoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(itemCarritoRepository, times(1)).findById(99L);
    }

    @Test
    void findByCarritoConResultados() {
        when(itemCarritoRepository.findByCarritoIdCarrito(1L)).thenReturn(List.of(itemCarrito));

        List<ItemCarrito> resultado = itemCarritoService.findByCarrito(1L);

        assertEquals(1, resultado.size());
        verify(itemCarritoRepository, times(1)).findByCarritoIdCarrito(1L);
    }

    @Test
    void findByCarritoSinResultados() {
        when(itemCarritoRepository.findByCarritoIdCarrito(99L)).thenReturn(List.of());

        List<ItemCarrito> resultado = itemCarritoService.findByCarrito(99L);

        assertTrue(resultado.isEmpty());
        verify(itemCarritoRepository, times(1)).findByCarritoIdCarrito(99L);
    }

    @Test
    void actualizarItemCarritoExistente() {
        ItemCarrito datos = new ItemCarrito(null, 10L, 3, 899990, LocalDateTime.of(2026, 10, 7, 9, 0), new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L));
        when(itemCarritoRepository.findById(1L)).thenReturn(Optional.of(itemCarrito));
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(new Carrito(1L, "12345678-5", "tok-abc123", LocalDateTime.of(2026, 10, 6, 11, 0), LocalDateTime.of(2026, 10, 6, 11, 30), EstadoCarrito.ACTIVO, 899990, 0, 899990, 2L)));
        when(restTemplate.getForObject(URI_STOCK, Map.class)).thenReturn(Map.of("stockDisponible", 5));
        when(itemCarritoRepository.save(any(ItemCarrito.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemCarrito resultado = itemCarritoService.actualizarItemCarrito(1L, datos);

        assertEquals(1L, resultado.getIdItemCarrito());
        assertEquals(3, resultado.getCantidad());
        verify(itemCarritoRepository, times(1)).findById(1L);
        verify(itemCarritoRepository, times(1)).save(itemCarrito);
    }

    @Test
    void actualizarItemCarritoNoExistente() {
        when(itemCarritoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> itemCarritoService.actualizarItemCarrito(99L, itemCarrito));

        assertEquals("ItemCarrito no encontrado con id: 99", ex.getMessage());
        verify(itemCarritoRepository, times(1)).findById(99L);
        verify(itemCarritoRepository, never()).save(any(ItemCarrito.class));
    }

    @Test
    void eliminarItemCarrito() {
        itemCarritoService.eliminarItemCarrito(1L);

        verify(itemCarritoRepository, times(1)).deleteById(1L);
    }

    @Test
    void guardarItemCarritoSinStockSuficiente() {
        when(restTemplate.getForEntity(URI.create(VARIANTES_URL + "/10"), String.class))
                .thenReturn(ResponseEntity.ok("{}"));
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(itemCarrito.getCarrito()));
        when(restTemplate.getForObject(URI_STOCK, Map.class)).thenReturn(Map.of("stockDisponible", 0));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> itemCarritoService.guardarItemCarrito(itemCarrito));

        assertEquals("Stock insuficiente para la variante con id: 10", ex.getMessage());
        verify(itemCarritoRepository, never()).save(any(ItemCarrito.class));
    }

    @Test
    void guardarItemCarritoSinRegistroDeInventario() {
        when(restTemplate.getForEntity(URI.create(VARIANTES_URL + "/10"), String.class))
                .thenReturn(ResponseEntity.ok("{}"));
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(itemCarrito.getCarrito()));
        when(restTemplate.getForObject(URI_STOCK, Map.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        assertThrows(IllegalStateException.class, () -> itemCarritoService.guardarItemCarrito(itemCarrito));

        verify(itemCarritoRepository, never()).save(any(ItemCarrito.class));
    }
}
