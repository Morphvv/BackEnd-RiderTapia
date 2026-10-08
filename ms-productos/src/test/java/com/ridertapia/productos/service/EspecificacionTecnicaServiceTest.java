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
import com.ridertapia.productos.model.EspecificacionTecnica;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.EstadoPublicacion;
import com.ridertapia.productos.repository.EspecificacionTecnicaRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@ExtendWith(MockitoExtension.class)
class EspecificacionTecnicaServiceTest {

    @Mock
    private EspecificacionTecnicaRepository especificacionTecnicaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private EspecificacionTecnicaService especificacionTecnicaService;

    private EspecificacionTecnica especificacionTecnica;

    @BeforeEach
    void setUp() {
        especificacionTecnica = new EspecificacionTecnica(1L, "Material del cuadro", "Aluminio", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
    }

    @Test
    void guardarEspecificacionTecnica() {
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(especificacionTecnicaRepository.save(especificacionTecnica)).thenReturn(especificacionTecnica);

        EspecificacionTecnica resultado = especificacionTecnicaService.guardarEspecificacionTecnica(especificacionTecnica);

        assertNotNull(resultado);
        assertEquals("Aluminio", resultado.getValor());
        verify(especificacionTecnicaRepository, times(1)).save(especificacionTecnica);
    }

    @Test
    void guardarEspecificacionTecnicaConProductoNoExistente() {
        especificacionTecnica.setProducto(new Producto(99L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> especificacionTecnicaService.guardarEspecificacionTecnica(especificacionTecnica));

        assertEquals("Producto no encontrado con id: 99", ex.getMessage());
        verify(especificacionTecnicaRepository, never()).save(any(EspecificacionTecnica.class));
    }

    @Test
    void listarEspecificacionesTecnicas() {
        EspecificacionTecnica otro = new EspecificacionTecnica(2L, "Peso", "14,5 kg", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(especificacionTecnicaRepository.findAll()).thenReturn(Arrays.asList(especificacionTecnica, otro));

        List<EspecificacionTecnica> resultado = especificacionTecnicaService.listarEspecificacionesTecnicas();

        assertEquals(2, resultado.size());
        verify(especificacionTecnicaRepository, times(1)).findAll();
    }

    @Test
    void findByIdExistente() {
        when(especificacionTecnicaRepository.findById(1L)).thenReturn(Optional.of(especificacionTecnica));

        Optional<EspecificacionTecnica> resultado = especificacionTecnicaService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Aluminio", resultado.get().getValor());
        verify(especificacionTecnicaRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdNoExistente() {
        when(especificacionTecnicaRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<EspecificacionTecnica> resultado = especificacionTecnicaService.findById(99L);

        assertTrue(resultado.isEmpty());
        verify(especificacionTecnicaRepository, times(1)).findById(99L);
    }

    @Test
    void findByProductoConResultados() {
        when(especificacionTecnicaRepository.findByProductoIdProducto(1L)).thenReturn(List.of(especificacionTecnica));

        List<EspecificacionTecnica> resultado = especificacionTecnicaService.findByProducto(1L);

        assertEquals(1, resultado.size());
        verify(especificacionTecnicaRepository, times(1)).findByProductoIdProducto(1L);
    }

    @Test
    void findByProductoSinResultados() {
        when(especificacionTecnicaRepository.findByProductoIdProducto(99L)).thenReturn(List.of());

        List<EspecificacionTecnica> resultado = especificacionTecnicaService.findByProducto(99L);

        assertTrue(resultado.isEmpty());
        verify(especificacionTecnicaRepository, times(1)).findByProductoIdProducto(99L);
    }

    @Test
    void actualizarEspecificacionTecnicaExistente() {
        EspecificacionTecnica datos = new EspecificacionTecnica(null, "Material del cuadro", "Carbono", new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>()));
        when(especificacionTecnicaRepository.findById(1L)).thenReturn(Optional.of(especificacionTecnica));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(new Producto(1L, "Bicicleta MTB Aro 29", "Cuadro de aluminio, 12 velocidades", 899990, EstadoPublicacion.PUBLICADO, true, 1, 2, LocalDateTime.of(2026, 10, 6, 10, 0), new Categoria(1L, "Bicicletas", "Bicicletas de todo tipo", true, null), new Marca(1L, "Trek", true), new ArrayList<>())));
        when(especificacionTecnicaRepository.save(any(EspecificacionTecnica.class))).thenAnswer(inv -> inv.getArgument(0));

        EspecificacionTecnica resultado = especificacionTecnicaService.actualizarEspecificacionTecnica(1L, datos);

        assertEquals(1L, resultado.getIdEspecificacionTecnica());
        assertEquals("Carbono", resultado.getValor());
        verify(especificacionTecnicaRepository, times(1)).findById(1L);
        verify(especificacionTecnicaRepository, times(1)).save(especificacionTecnica);
    }

    @Test
    void actualizarEspecificacionTecnicaNoExistente() {
        when(especificacionTecnicaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> especificacionTecnicaService.actualizarEspecificacionTecnica(99L, especificacionTecnica));

        assertEquals("EspecificacionTecnica no encontrada con id: 99", ex.getMessage());
        verify(especificacionTecnicaRepository, times(1)).findById(99L);
        verify(especificacionTecnicaRepository, never()).save(any(EspecificacionTecnica.class));
    }

    @Test
    void eliminarEspecificacionTecnica() {
        especificacionTecnicaService.eliminarEspecificacionTecnica(1L);

        verify(especificacionTecnicaRepository, times(1)).deleteById(1L);
    }
}
