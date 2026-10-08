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
import com.ridertapia.productos.model.ImagenProducto;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.ImagenProductoRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class ImagenProductoServiceTest {

    @Mock
    private ImagenProductoRepository imagenProductoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ImagenProductoService imagenProductoService;

    private ImagenProducto imagenProducto;

    @BeforeEach
    void setUp() {
        imagenProducto = new ImagenProducto(1L, "https://img.ridertapia.cl/mtb29-1.jpg", 1, true, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void guardarImagenProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(imagenProductoRepository.save(imagenProducto)).thenReturn(imagenProducto);

        ImagenProducto resultado = imagenProductoService.guardarImagenProducto(imagenProducto);

        assertNotNull(resultado);
        assertEquals("https://img.ridertapia.cl/mtb29-1.jpg", resultado.getUrl());
        verify(imagenProductoRepository, times(1)).save(imagenProducto);
    }

    @Test
    void guardarImagenProductoConProductoNoExistente() {
        imagenProducto.setProducto(new Producto(99L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> imagenProductoService.guardarImagenProducto(imagenProducto));

        assertEquals("Producto no encontrado con id: 99", ex.getMessage());
        verify(imagenProductoRepository, never()).save(any(ImagenProducto.class));
    }

    @Test
    void listarImagenesProducto() {
        ImagenProducto otro = new ImagenProducto(2L, "https://img.ridertapia.cl/mtb29-2.jpg", 2, false, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(imagenProductoRepository.findAll()).thenReturn(Arrays.asList(imagenProducto, otro));

        List<ImagenProducto> resultado = imagenProductoService.listarImagenesProducto();

        assertEquals(2, resultado.size());
        verify(imagenProductoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(imagenProductoRepository.findById(1L)).thenReturn(Optional.of(imagenProducto));

        Optional<ImagenProducto> resultado = imagenProductoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("https://img.ridertapia.cl/mtb29-1.jpg", resultado.get().getUrl());
        verify(imagenProductoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(imagenProductoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<ImagenProducto> resultado = imagenProductoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(imagenProductoRepository, times(1)).findById(99L);
    }

    @Test
    void findByProductoConResultados() {
        when(imagenProductoRepository.findByProductoIdProducto(1L)).thenReturn(List.of(imagenProducto));

        List<ImagenProducto> resultado = imagenProductoService.findByProducto(1L);

        assertEquals(1, resultado.size());
        verify(imagenProductoRepository, times(1)).findByProductoIdProducto(1L);
    }

    @Test
    void findByProductoSinResultados() {
        when(imagenProductoRepository.findByProductoIdProducto(99L)).thenReturn(List.of());

        List<ImagenProducto> resultado = imagenProductoService.findByProducto(99L);

        assertTrue(resultado.isEmpty());
        verify(imagenProductoRepository, times(1)).findByProductoIdProducto(99L);
    }

    @Test
    void actualizarImagenProductoExistente() {
        ImagenProducto datos = new ImagenProducto(null, "https://img.ridertapia.cl/mtb29-3.jpg", 3, false, new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(imagenProductoRepository.findById(1L)).thenReturn(Optional.of(imagenProducto));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(imagenProductoRepository.save(any(ImagenProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        ImagenProducto resultado = imagenProductoService.actualizarImagenProducto(1L, datos);

        assertEquals(1L, resultado.getIdImagenProducto());
        assertEquals("https://img.ridertapia.cl/mtb29-3.jpg", resultado.getUrl());
        verify(imagenProductoRepository, times(1)).findById(1L);
        verify(imagenProductoRepository, times(1)).save(imagenProducto);
    }

    @Test
    void actualizarImagenProductoNoExistente() {
        when(imagenProductoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> imagenProductoService.actualizarImagenProducto(99L, imagenProducto));

        assertEquals("ImagenProducto no encontrada con id: 99", ex.getMessage());
        verify(imagenProductoRepository, times(1)).findById(99L);
        verify(imagenProductoRepository, never()).save(any(ImagenProducto.class));
    }

    @Test
    void eliminarImagenProducto() {
        imagenProductoService.eliminarImagenProducto(1L);

        verify(imagenProductoRepository, times(1)).deleteById(1L);
    }
}
