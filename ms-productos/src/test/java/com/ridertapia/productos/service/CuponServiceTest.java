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

import java.time.LocalDate;
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
import com.ridertapia.productos.model.Cupon;
import com.ridertapia.productos.model.TipoDescuento;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.CuponRepository;

@ExtendWith(MockitoExtension.class)
class CuponServiceTest {

    @Mock
    private CuponRepository cuponRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CuponService cuponService;

    private Cupon cupon;

    @BeforeEach
    void setUp() {
        cupon = new Cupon(1L, "RIDER10", TipoDescuento.PORCENTAJE, 10, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 100, 0, true, new ArrayList<>());
    }

    @Test
    void guardarCupon() {
        when(cuponRepository.save(cupon)).thenReturn(cupon);

        Cupon resultado = cuponService.guardarCupon(cupon);

        assertNotNull(resultado);
        assertEquals(10, resultado.getValor());
        verify(cuponRepository, times(1)).save(cupon);
    }

    @Test
    void guardarCuponConCategoriasNoExistente() {
        cupon.setCategorias(new ArrayList<>(List.of(new Categoria(99L, "Bicicletas", "Bicicletas de todo tipo", true, null))));
        when(categoriaRepository.findAllById(List.of(99L))).thenReturn(List.of());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> cuponService.guardarCupon(cupon));

        assertEquals("Categoria no encontrada en: [99]", ex.getMessage());
        verify(cuponRepository, never()).save(any(Cupon.class));
    }

    @Test
    void listarCupones() {
        Cupon otro = new Cupon(2L, "BIENVENIDA5000", TipoDescuento.MONTO_FIJO, 5000, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 50, 0, true, new ArrayList<>());
        when(cuponRepository.findAll()).thenReturn(Arrays.asList(cupon, otro));

        List<Cupon> resultado = cuponService.listarCupones();

        assertEquals(2, resultado.size());
        verify(cuponRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(cuponRepository.findById(1L)).thenReturn(Optional.of(cupon));

        Optional<Cupon> resultado = cuponService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(10, resultado.get().getValor());
        verify(cuponRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(cuponRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Cupon> resultado = cuponService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(cuponRepository, times(1)).findById(99L);
    }

    @Test
    void findByCodigoExistente() {
        when(cuponRepository.findByCodigo("RIDER10")).thenReturn(Optional.of(cupon));

        Cupon resultado = cuponService.findByCodigo("RIDER10");

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdCupon());
        verify(cuponRepository, times(1)).findByCodigo("RIDER10");
    }

    @Test
    void findByCodigoNoExistente() {
        when(cuponRepository.findByCodigo("NO-EXISTE")).thenReturn(Optional.empty());

        Cupon resultado = cuponService.findByCodigo("NO-EXISTE");

        assertNull(resultado);
        verify(cuponRepository, times(1)).findByCodigo("NO-EXISTE");
    }

    @Test
    void actualizarCuponExistente() {
        Cupon datos = new Cupon(null, "RIDER15", TipoDescuento.PORCENTAJE, 15, LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 30), 200, 10, false, new ArrayList<>());
        when(cuponRepository.findById(1L)).thenReturn(Optional.of(cupon));
        when(cuponRepository.save(any(Cupon.class))).thenAnswer(inv -> inv.getArgument(0));

        Cupon resultado = cuponService.actualizarCupon(1L, datos);

        assertEquals(1L, resultado.getIdCupon());
        assertEquals(15, resultado.getValor());
        verify(cuponRepository, times(1)).findById(1L);
        verify(cuponRepository, times(1)).save(cupon);
    }

    @Test
    void actualizarCuponNoExistente() {
        when(cuponRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cuponService.actualizarCupon(99L, cupon));

        assertEquals("Cupon no encontrado con id: 99", ex.getMessage());
        verify(cuponRepository, times(1)).findById(99L);
        verify(cuponRepository, never()).save(any(Cupon.class));
    }

    @Test
    void eliminarCupon() {
        cuponService.eliminarCupon(1L);

        verify(cuponRepository, times(1)).deleteById(1L);
    }
}
