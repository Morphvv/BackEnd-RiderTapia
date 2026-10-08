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

import com.ridertapia.productos.repository.CategoriaRepository;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null);
    }

    @Test
    void guardarCategoria() {
        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria resultado = categoriaService.guardarCategoria(categoria);

        assertNotNull(resultado);
        assertEquals("Bicicletas", resultado.getNombre());
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    void guardarCategoriaConCategoriaPadreNoExistente() {
        categoria.setCategoriaPadre(new Categoria(99L, "Bicicletas", "Bicicletas de todo tipo", true, null));
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> categoriaService.guardarCategoria(categoria));

        assertEquals("Categoria no encontrada con id: 99", ex.getMessage());
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void listarCategorias() {
        Categoria otro = new Categoria(2L, "Accesorios", "Cascos, luces y repuestos", true, null);
        when(categoriaRepository.findAll()).thenReturn(Arrays.asList(categoria, otro));

        List<Categoria> resultado = categoriaService.listarCategorias();

        assertEquals(2, resultado.size());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Optional<Categoria> resultado = categoriaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Bicicletas", resultado.get().getNombre());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Categoria> resultado = categoriaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(categoriaRepository, times(1)).findById(99L);
    }

    @Test
    void findByNombreExistente() {
        when(categoriaRepository.findByNombre("Bicicletas")).thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaService.findByNombre("Bicicletas");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdCategoria());
        verify(categoriaRepository, times(1)).findByNombre("Bicicletas");
    }

    @Test
    void findByNombreNoExistente() {
        when(categoriaRepository.findByNombre("NO-EXISTE")).thenReturn(Optional.empty());

        Categoria resultado = categoriaService.findByNombre("NO-EXISTE");

        assertNull(resultado);
        verify(categoriaRepository, times(1)).findByNombre("NO-EXISTE");
    }

    @Test
    void actualizarCategoriaExistente() {
        Categoria datos = new Categoria(null, "Bicicletas MTB", "Bicicletas de montaña", false, null);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria resultado = categoriaService.actualizarCategoria(1L, datos);

        assertEquals(1L, resultado.getIdCategoria());
        assertEquals("Bicicletas MTB", resultado.getNombre());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).save(categoria);
    }

    @Test
    void actualizarCategoriaNoExistente() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> categoriaService.actualizarCategoria(99L, categoria));

        assertEquals("Categoria no encontrada con id: 99", ex.getMessage());
        verify(categoriaRepository, times(1)).findById(99L);
        verify(categoriaRepository, never()).save(any(Categoria.class));
    }

    @Test
    void eliminarCategoria() {
        categoriaService.eliminarCategoria(1L);

        verify(categoriaRepository, times(1)).deleteById(1L);
    }
}
