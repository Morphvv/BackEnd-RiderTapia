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
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.Resena;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.ProductoRepository;
import com.ridertapia.productos.repository.ResenaRepository;

@ExtendWith(MockitoExtension.class)
class ResenaServiceTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ResenaService resenaService;

    private Resena resena;

    @BeforeEach
    void setUp() {
        resena = new Resena(1L, 3L, 5, "Excelente bicicleta", LocalDateTime.of(2026, 10, 6, 10, 0), true, "Gracias por tu compra", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void guardarResena() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(resenaRepository.save(resena)).thenReturn(resena);

        Resena resultado = resenaService.guardarResena(resena);

        assertNotNull(resultado);
        assertEquals(5, resultado.getCalificacion());
        verify(resenaRepository, times(1)).save(resena);
    }

    @Test
    void guardarResenaConProductoNoExistente() {
        resena.setProducto(new Producto(99L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> resenaService.guardarResena(resena));

        assertEquals("Producto no encontrado con id: 99", ex.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    void listarResenas() {
        Resena otro = new Resena(2L, 4L, 4, "Buena relación precio calidad", LocalDateTime.of(2026, 10, 6, 12, 0), true, "Gracias por comentar", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(resenaRepository.findAll()).thenReturn(Arrays.asList(resena, otro));

        List<Resena> resultado = resenaService.listarResenas();

        assertEquals(2, resultado.size());
        verify(resenaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));

        Optional<Resena> resultado = resenaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(5, resultado.get().getCalificacion());
        verify(resenaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(resenaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Resena> resultado = resenaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(resenaRepository, times(1)).findById(99L);
    }

    @Test
    void findByProductoConResultados() {
        when(resenaRepository.findByProductoIdProducto(1L)).thenReturn(List.of(resena));

        List<Resena> resultado = resenaService.findByProducto(1L);

        assertEquals(1, resultado.size());
        verify(resenaRepository, times(1)).findByProductoIdProducto(1L);
    }

    @Test
    void findByProductoSinResultados() {
        when(resenaRepository.findByProductoIdProducto(99L)).thenReturn(List.of());

        List<Resena> resultado = resenaService.findByProducto(99L);

        assertTrue(resultado.isEmpty());
        verify(resenaRepository, times(1)).findByProductoIdProducto(99L);
    }

    @Test
    void actualizarResenaExistente() {
        Resena datos = new Resena(null, 3L, 3, "Cambié de opinión", LocalDateTime.of(2026, 10, 7, 9, 0), false, "Lamentamos tu experiencia", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(resenaRepository.findById(1L)).thenReturn(Optional.of(resena));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(resenaRepository.save(any(Resena.class))).thenAnswer(inv -> inv.getArgument(0));

        Resena resultado = resenaService.actualizarResena(1L, datos);

        assertEquals(1L, resultado.getIdResena());
        assertEquals(3, resultado.getCalificacion());
        verify(resenaRepository, times(1)).findById(1L);
        verify(resenaRepository, times(1)).save(resena);
    }

    @Test
    void actualizarResenaNoExistente() {
        when(resenaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> resenaService.actualizarResena(99L, resena));

        assertEquals("Resena no encontrada con id: 99", ex.getMessage());
        verify(resenaRepository, times(1)).findById(99L);
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    void eliminarResena() {
        resenaService.eliminarResena(1L);

        verify(resenaRepository, times(1)).deleteById(1L);
    }
}
