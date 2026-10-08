package com.ridertapia.inventario.service;

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

import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.EstadoStock;
import com.ridertapia.inventario.repository.ItemInventarioRepository;

@ExtendWith(MockitoExtension.class)
class ItemInventarioServiceTest {

    @Mock
    private ItemInventarioRepository itemInventarioRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ItemInventarioService itemInventarioService;

    private static final String VARIANTES_URL = "http://localhost:8085/api/v1/variantes";

    private ItemInventario itemInventario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(itemInventarioService, "varianteUrl", VARIANTES_URL);
        itemInventario = new ItemInventario(1L, 10L, 15, 2, 3, 650000, LocalDateTime.of(2026, 10, 6, 9, 0), new ArrayList<>(List.of(1L, 2L)));
    }

    @Test
    void guardarItemInventario() {
        when(restTemplate.getForEntity(URI.create(VARIANTES_URL + "/10"), String.class))
                .thenReturn(ResponseEntity.ok("{}"));
        when(itemInventarioRepository.save(itemInventario)).thenReturn(itemInventario);

        ItemInventario resultado = itemInventarioService.guardarItemInventario(itemInventario);

        assertNotNull(resultado);
        assertEquals(15, resultado.getStockDisponible());
        verify(itemInventarioRepository, times(1)).save(itemInventario);
    }

    @Test
    void guardarItemInventarioConVarianteNoExistente() {
        when(restTemplate.getForEntity(URI.create(VARIANTES_URL + "/10"), String.class))
                .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                        new HttpHeaders(), new byte[0], StandardCharsets.UTF_8));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> itemInventarioService.guardarItemInventario(itemInventario));

        assertEquals("Variante no encontrada con id: 10", ex.getMessage());
        verify(itemInventarioRepository, never()).save(any(ItemInventario.class));
    }

    @Test
    void listarItemsInventario() {
        ItemInventario otro = new ItemInventario(2L, 11L, 0, 0, 3, 45000, LocalDateTime.of(2026, 10, 6, 12, 0), new ArrayList<>(List.of(3L)));
        when(itemInventarioRepository.findAll()).thenReturn(Arrays.asList(itemInventario, otro));

        List<ItemInventario> resultado = itemInventarioService.listarItemsInventario();

        assertEquals(2, resultado.size());
        verify(itemInventarioRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(itemInventario));

        Optional<ItemInventario> resultado = itemInventarioService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(15, resultado.get().getStockDisponible());
        verify(itemInventarioRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(itemInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ItemInventario> resultado = itemInventarioService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(itemInventarioRepository, times(1)).findById(99L);
    }

    @Test
    void findByIdVarianteExistente() {
        when(itemInventarioRepository.findByIdVariante(10L)).thenReturn(Optional.of(itemInventario));

        ItemInventario resultado = itemInventarioService.findByIdVariante(10L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdItemInventario());
        verify(itemInventarioRepository, times(1)).findByIdVariante(10L);
    }

    @Test
    void findByIdVarianteNoExistente() {
        when(itemInventarioRepository.findByIdVariante(99L)).thenReturn(Optional.empty());

        ItemInventario resultado = itemInventarioService.findByIdVariante(99L);

        assertNull(resultado);
        verify(itemInventarioRepository, times(1)).findByIdVariante(99L);
    }

    @Test
    void actualizarItemInventarioExistente() {
        ItemInventario datos = new ItemInventario(null, 12L, 12, 5, 4, 640000, LocalDateTime.of(2026, 10, 7, 9, 0), new ArrayList<>(List.of(1L)));
        when(itemInventarioRepository.findById(1L)).thenReturn(Optional.of(itemInventario));
        when(itemInventarioRepository.save(any(ItemInventario.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemInventario resultado = itemInventarioService.actualizarItemInventario(1L, datos);

        assertEquals(1L, resultado.getIdItemInventario());
        assertEquals(12, resultado.getStockDisponible());
        verify(itemInventarioRepository, times(1)).findById(1L);
        verify(itemInventarioRepository, times(1)).save(itemInventario);
    }

    @Test
    void actualizarItemInventarioNoExistente() {
        when(itemInventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> itemInventarioService.actualizarItemInventario(99L, itemInventario));

        assertEquals("ItemInventario no encontrado con id: 99", ex.getMessage());
        verify(itemInventarioRepository, times(1)).findById(99L);
        verify(itemInventarioRepository, never()).save(any(ItemInventario.class));
    }

    @Test
    void eliminarItemInventario() {
        itemInventarioService.eliminarItemInventario(1L);

        verify(itemInventarioRepository, times(1)).deleteById(1L);
    }

    @Test
    void calcularStockTotalYEstado() {
        assertEquals(17, itemInventario.getStockTotal());
        assertEquals(EstadoStock.CON_STOCK, itemInventario.getEstadoStock());

        itemInventario.setStockDisponible(2);
        assertEquals(EstadoStock.BAJO_MINIMO, itemInventario.getEstadoStock());

        itemInventario.setStockDisponible(0);
        assertEquals(EstadoStock.SIN_STOCK, itemInventario.getEstadoStock());
    }
}
