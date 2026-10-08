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

import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Compatibilidad;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.CompatibilidadRepository;
import com.ridertapia.productos.repository.MarcaRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private CompatibilidadRepository compatibilidadRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>());
    }

    @Test
    void guardarProducto() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null)));
        when(marcaRepository.findById(1L)).thenReturn(Optional.of(new Marca(1L, "Trek", true)));
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.guardarProducto(producto);

        assertNotNull(resultado);
        assertEquals(899990, resultado.getPrecioNormal());
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void guardarProductoConCategoriaNoExistente() {
        producto.setCategoria(new Categoria(99L, "Bicicletas", "Bicicletas de todo tipo", true, null));
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> productoService.guardarProducto(producto));

        assertEquals("Categoria no encontrada con id: 99", ex.getMessage());
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void listarProductos() {
        Producto otro = new Producto(2L, "Casco Enduro", "Casco con visera", 79990, EstadoPublicacion.PUBLICADO, false, 0, 5, LocalDateTime.of(2026, 10, 6, 12, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>());
        when(productoRepository.findAll()).thenReturn(Arrays.asList(producto, otro));

        List<Producto> resultado = productoService.listarProductos();

        assertEquals(2, resultado.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        Optional<Producto> resultado = productoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(899990, resultado.get().getPrecioNormal());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Producto> resultado = productoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(productoRepository, times(1)).findById(99L);
    }

    @Test
    void findByCategoriaConResultados() {
        when(productoRepository.findByCategoriaIdCategoria(1L)).thenReturn(List.of(producto));

        List<Producto> resultado = productoService.findByCategoria(1L);

        assertEquals(1, resultado.size());
        verify(productoRepository, times(1)).findByCategoriaIdCategoria(1L);
    }

    @Test
    void findByCategoriaSinResultados() {
        when(productoRepository.findByCategoriaIdCategoria(99L)).thenReturn(List.of());

        List<Producto> resultado = productoService.findByCategoria(99L);

        assertTrue(resultado.isEmpty());
        verify(productoRepository, times(1)).findByCategoriaIdCategoria(99L);
    }

    @Test
    void actualizarProductoExistente() {
        Producto datos = new Producto(null, "Bicicleta MTB Aro 29 Pro", "Cuadro de carbono", 1099990, EstadoPublicacion.OCULTO, true, 2, 1, LocalDateTime.of(2026, 10, 7, 9, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>());
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null)));
        when(marcaRepository.findById(1L)).thenReturn(Optional.of(new Marca(1L, "Trek", true)));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto resultado = productoService.actualizarProducto(1L, datos);

        assertEquals(1L, resultado.getIdProducto());
        assertEquals(1099990, resultado.getPrecioNormal());
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(producto);
    }

    @Test
    void actualizarProductoNoExistente() {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productoService.actualizarProducto(99L, producto));

        assertEquals("Producto no encontrado con id: 99", ex.getMessage());
        verify(productoRepository, times(1)).findById(99L);
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    void eliminarProducto() {
        productoService.eliminarProducto(1L);

        verify(productoRepository, times(1)).deleteById(1L);
    }
}
