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
import com.ridertapia.productos.model.Oferta;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.OfertaRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    @Mock
    private OfertaRepository ofertaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private OfertaService ofertaService;

    private Oferta oferta;

    @BeforeEach
    void setUp() {
        oferta = new Oferta(1L, 799990, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void guardarOferta() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(ofertaRepository.save(oferta)).thenReturn(oferta);

        Oferta resultado = ofertaService.guardarOferta(oferta);

        assertNotNull(resultado);
        assertEquals(799990, resultado.getPrecioOferta());
        verify(ofertaRepository, times(1)).save(oferta);
    }

    @Test
    void guardarOfertaConProductoNoExistente() {
        oferta.setProducto(new Producto(99L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> ofertaService.guardarOferta(oferta));

        assertEquals("Producto no encontrado con id: 99", ex.getMessage());
        verify(ofertaRepository, never()).save(any(Oferta.class));
    }

    @Test
    void listarOfertas() {
        Oferta otro = new Oferta(2L, 749990, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(ofertaRepository.findAll()).thenReturn(Arrays.asList(oferta, otro));

        List<Oferta> resultado = ofertaService.listarOfertas();

        assertEquals(2, resultado.size());
        verify(ofertaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(ofertaRepository.findById(1L)).thenReturn(Optional.of(oferta));

        Optional<Oferta> resultado = ofertaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals(799990, resultado.get().getPrecioOferta());
        verify(ofertaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(ofertaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Oferta> resultado = ofertaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(ofertaRepository, times(1)).findById(99L);
    }

    @Test
    void findByProductoConResultados() {
        when(ofertaRepository.findByProductoIdProducto(1L)).thenReturn(List.of(oferta));

        List<Oferta> resultado = ofertaService.findByProducto(1L);

        assertEquals(1, resultado.size());
        verify(ofertaRepository, times(1)).findByProductoIdProducto(1L);
    }

    @Test
    void findByProductoSinResultados() {
        when(ofertaRepository.findByProductoIdProducto(99L)).thenReturn(List.of());

        List<Oferta> resultado = ofertaService.findByProducto(99L);

        assertTrue(resultado.isEmpty());
        verify(ofertaRepository, times(1)).findByProductoIdProducto(99L);
    }

    @Test
    void actualizarOfertaExistente() {
        Oferta datos = new Oferta(null, 699990, LocalDate.of(2026, 10, 31), LocalDate.of(2026, 11, 30), new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(ofertaRepository.findById(1L)).thenReturn(Optional.of(oferta));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(ofertaRepository.save(any(Oferta.class))).thenAnswer(inv -> inv.getArgument(0));

        Oferta resultado = ofertaService.actualizarOferta(1L, datos);

        assertEquals(1L, resultado.getIdOferta());
        assertEquals(699990, resultado.getPrecioOferta());
        verify(ofertaRepository, times(1)).findById(1L);
        verify(ofertaRepository, times(1)).save(oferta);
    }

    @Test
    void actualizarOfertaNoExistente() {
        when(ofertaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ofertaService.actualizarOferta(99L, oferta));

        assertEquals("Oferta no encontrada con id: 99", ex.getMessage());
        verify(ofertaRepository, times(1)).findById(99L);
        verify(ofertaRepository, never()).save(any(Oferta.class));
    }

    @Test
    void eliminarOferta() {
        ofertaService.eliminarOferta(1L);

        verify(ofertaRepository, times(1)).deleteById(1L);
    }
}
