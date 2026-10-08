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
import com.ridertapia.productos.model.VarianteProducto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.ProductoRepository;
import com.ridertapia.productos.repository.VarianteProductoRepository;

@ExtendWith(MockitoExtension.class)
class VarianteProductoServiceTest {

    @Mock
    private VarianteProductoRepository varianteProductoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VarianteProductoService varianteProductoService;

    private VarianteProducto varianteProducto;

    @BeforeEach
    void setUp() {
        varianteProducto = new VarianteProducto(1L, "BIC-MTB29-M-ROJ", "M", "Rojo", "29", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void guardarVarianteProducto() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(varianteProductoRepository.save(varianteProducto)).thenReturn(varianteProducto);

        VarianteProducto resultado = varianteProductoService.guardarVarianteProducto(varianteProducto);

        assertNotNull(resultado);
        assertEquals("Rojo", resultado.getColor());
        verify(varianteProductoRepository, times(1)).save(varianteProducto);
    }

    @Test
    void guardarVarianteProductoConProductoNoExistente() {
        varianteProducto.setProducto(new Producto(99L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> varianteProductoService.guardarVarianteProducto(varianteProducto));

        assertEquals("Producto no encontrado con id: 99", ex.getMessage());
        verify(varianteProductoRepository, never()).save(any(VarianteProducto.class));
    }

    @Test
    void listarVariantesProducto() {
        VarianteProducto otro = new VarianteProducto(2L, "BIC-MTB29-L-NEG", "L", "Negro", "29", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(varianteProductoRepository.findAll()).thenReturn(Arrays.asList(varianteProducto, otro));

        List<VarianteProducto> resultado = varianteProductoService.listarVariantesProducto();

        assertEquals(2, resultado.size());
        verify(varianteProductoRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(varianteProducto));

        Optional<VarianteProducto> resultado = varianteProductoService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Rojo", resultado.get().getColor());
        verify(varianteProductoRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(varianteProductoRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<VarianteProducto> resultado = varianteProductoService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(varianteProductoRepository, times(1)).findById(99L);
    }

    @Test
    void findBySkuExistente() {
        when(varianteProductoRepository.findBySku("BIC-MTB29-M-ROJ")).thenReturn(Optional.of(varianteProducto));

        VarianteProducto resultado = varianteProductoService.findBySku("BIC-MTB29-M-ROJ");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdVarianteProducto());
        verify(varianteProductoRepository, times(1)).findBySku("BIC-MTB29-M-ROJ");
    }

    @Test
    void findBySkuNoExistente() {
        when(varianteProductoRepository.findBySku("NO-EXISTE")).thenReturn(Optional.empty());

        VarianteProducto resultado = varianteProductoService.findBySku("NO-EXISTE");

        assertNull(resultado);
        verify(varianteProductoRepository, times(1)).findBySku("NO-EXISTE");
    }

    @Test
    void actualizarVarianteProductoExistente() {
        VarianteProducto datos = new VarianteProducto(null, "BIC-MTB29-M-AZU", "M", "Azul", "27.5", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(varianteProductoRepository.findById(1L)).thenReturn(Optional.of(varianteProducto));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(varianteProductoRepository.save(any(VarianteProducto.class))).thenAnswer(inv -> inv.getArgument(0));

        VarianteProducto resultado = varianteProductoService.actualizarVarianteProducto(1L, datos);

        assertEquals(1L, resultado.getIdVarianteProducto());
        assertEquals("Azul", resultado.getColor());
        verify(varianteProductoRepository, times(1)).findById(1L);
        verify(varianteProductoRepository, times(1)).save(varianteProducto);
    }

    @Test
    void actualizarVarianteProductoNoExistente() {
        when(varianteProductoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> varianteProductoService.actualizarVarianteProducto(99L, varianteProducto));

        assertEquals("VarianteProducto no encontrada con id: 99", ex.getMessage());
        verify(varianteProductoRepository, times(1)).findById(99L);
        verify(varianteProductoRepository, never()).save(any(VarianteProducto.class));
    }

    @Test
    void eliminarVarianteProducto() {
        varianteProductoService.eliminarVarianteProducto(1L);

        verify(varianteProductoRepository, times(1)).deleteById(1L);
    }
}
