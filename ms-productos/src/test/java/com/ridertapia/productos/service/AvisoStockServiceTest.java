package com.ridertapia.productos.service;

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

import com.ridertapia.productos.model.AvisoStock;
import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.AvisoStockRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class AvisoStockServiceTest {

    @Mock
    private AvisoStockRepository avisoStockRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private AvisoStockService avisoStockService;

    private AvisoStock avisoStock;

    @BeforeEach
    void setUp() {
        avisoStock = new AvisoStock(1L, "cliente@correo.cl", LocalDateTime.of(2026, 10, 6, 10, 0), false, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void guardarAvisoStock() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(avisoStockRepository.save(avisoStock)).thenReturn(avisoStock);

        AvisoStock resultado = avisoStockService.guardarAvisoStock(avisoStock);

        assertNotNull(resultado);
        assertEquals("cliente@correo.cl", resultado.getCorreo());
        verify(avisoStockRepository, times(1)).save(avisoStock);
    }

    @Test
    void guardarAvisoStockConProductoNoExistente() {
        avisoStock.setProducto(new Producto(99L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> avisoStockService.guardarAvisoStock(avisoStock));

        assertEquals("Producto no encontrado con id: 99", ex.getMessage());
        verify(avisoStockRepository, never()).save(any(AvisoStock.class));
    }

    @Test
    void listarAvisosStock() {
        AvisoStock otro = new AvisoStock(2L, "otro@correo.cl", LocalDateTime.of(2026, 10, 6, 12, 0), false, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(avisoStockRepository.findAll()).thenReturn(Arrays.asList(avisoStock, otro));

        List<AvisoStock> resultado = avisoStockService.listarAvisosStock();

        assertEquals(2, resultado.size());
        verify(avisoStockRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(avisoStockRepository.findById(1L)).thenReturn(Optional.of(avisoStock));

        Optional<AvisoStock> resultado = avisoStockService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("cliente@correo.cl", resultado.get().getCorreo());
        verify(avisoStockRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(avisoStockRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<AvisoStock> resultado = avisoStockService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(avisoStockRepository, times(1)).findById(99L);
    }

    @Test
    void findByProductoConResultados() {
        when(avisoStockRepository.findByProductoIdProducto(1L)).thenReturn(List.of(avisoStock));

        List<AvisoStock> resultado = avisoStockService.findByProducto(1L);

        assertEquals(1, resultado.size());
        verify(avisoStockRepository, times(1)).findByProductoIdProducto(1L);
    }

    @Test
    void findByProductoSinResultados() {
        when(avisoStockRepository.findByProductoIdProducto(99L)).thenReturn(List.of());

        List<AvisoStock> resultado = avisoStockService.findByProducto(99L);

        assertTrue(resultado.isEmpty());
        verify(avisoStockRepository, times(1)).findByProductoIdProducto(99L);
    }

    @Test
    void actualizarAvisoStockExistente() {
        AvisoStock datos = new AvisoStock(null, "nuevo@correo.cl", LocalDateTime.of(2026, 10, 7, 9, 0), true, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(avisoStockRepository.findById(1L)).thenReturn(Optional.of(avisoStock));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(avisoStockRepository.save(any(AvisoStock.class))).thenAnswer(inv -> inv.getArgument(0));

        AvisoStock resultado = avisoStockService.actualizarAvisoStock(1L, datos);

        assertEquals(1L, resultado.getIdAvisoStock());
        assertEquals("nuevo@correo.cl", resultado.getCorreo());
        verify(avisoStockRepository, times(1)).findById(1L);
        verify(avisoStockRepository, times(1)).save(avisoStock);
    }

    @Test
    void actualizarAvisoStockNoExistente() {
        when(avisoStockRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> avisoStockService.actualizarAvisoStock(99L, avisoStock));

        assertEquals("AvisoStock no encontrado con id: 99", ex.getMessage());
        verify(avisoStockRepository, times(1)).findById(99L);
        verify(avisoStockRepository, never()).save(any(AvisoStock.class));
    }

    @Test
    void eliminarAvisoStock() {
        avisoStockService.eliminarAvisoStock(1L);

        verify(avisoStockRepository, times(1)).deleteById(1L);
    }
}
