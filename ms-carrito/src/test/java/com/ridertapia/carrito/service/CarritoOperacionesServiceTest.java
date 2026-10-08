package com.ridertapia.carrito.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.carrito.dto.AvisoCambio;
import com.ridertapia.carrito.dto.ResultadoValidacionCupon;
import com.ridertapia.carrito.dto.ResumenCarrito;
import com.ridertapia.carrito.model.Carrito;
import com.ridertapia.carrito.model.EstadoCarrito;
import com.ridertapia.carrito.model.ItemCarrito;
import com.ridertapia.carrito.model.TipoAviso;
import com.ridertapia.carrito.repository.CarritoRepository;
import com.ridertapia.carrito.repository.ItemCarritoRepository;

// Tests de las operaciones del carrito del diagrama: getResumen(), revalidar() y aplicarCupon()
@ExtendWith(MockitoExtension.class)
class CarritoOperacionesServiceTest {

    private static final String INVENTARIO_URL = "http://localhost:8088/api/v1/items-inventario";
    private static final String CUPONES_URL = "http://localhost:8085/api/v1/cupones";

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ItemCarritoRepository itemCarritoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CarritoService carritoService;

    private Carrito carrito;
    private ItemCarrito bicicleta;
    private ItemCarrito casco;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(carritoService, "inventarioUrl", INVENTARIO_URL);
        ReflectionTestUtils.setField(carritoService, "cuponesUrl", CUPONES_URL);
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 6, 11, 0);
        carrito = new Carrito(1L, "12345678-5", null, ahora, ahora, EstadoCarrito.ACTIVO, 979980, 0, 979980, null);
        bicicleta = new ItemCarrito(1L, 10L, 1, 899990, ahora, carrito);
        casco = new ItemCarrito(2L, 11L, 2, 39995, ahora, carrito);
    }

    private HttpClientErrorException notFound() {
        return HttpClientErrorException.create(HttpStatus.NOT_FOUND, "Not Found",
                new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
    }

    private Map<String, Object> cupon(String tipo, int valor, boolean activo, String fechaTermino, int usos, int limite) {
        Map<String, Object> cupon = new HashMap<>();
        cupon.put("tipoDescuento", tipo);
        cupon.put("valor", valor);
        cupon.put("activo", activo);
        cupon.put("fechaTermino", fechaTermino);
        cupon.put("usosActuales", usos);
        cupon.put("limiteUsos", limite);
        return cupon;
    }

    @Test
    void obtenerResumen() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoIdCarrito(1L)).thenReturn(List.of(bicicleta, casco));

        ResumenCarrito resumen = carritoService.obtenerResumen(1L);

        assertEquals(3, resumen.getCantidadItems());
        assertEquals(979980, resumen.getTotal());
    }

    @Test
    void obtenerResumenConDescuento() {
        carrito.setDescuento(97998);
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoIdCarrito(1L)).thenReturn(List.of(bicicleta, casco));

        assertEquals(881982, carritoService.obtenerResumen(1L).getTotal());
    }

    @Test
    void obtenerResumenCarritoNoExistente() {
        when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> carritoService.obtenerResumen(99L));

        assertEquals("Carrito no encontrado con id: 99", ex.getMessage());
    }

    @Test
    void revalidarGeneraAvisos() {
        ItemCarrito luces = new ItemCarrito(3L, 12L, 1, 15990, bicicleta.getFechaAgregado(), carrito);
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoIdCarrito(1L)).thenReturn(List.of(bicicleta, casco, luces));
        when(restTemplate.getForObject(URI.create(INVENTARIO_URL + "/idVariante/10"), Map.class))
                .thenReturn(Map.of("stockDisponible", 0));
        when(restTemplate.getForObject(URI.create(INVENTARIO_URL + "/idVariante/11"), Map.class))
                .thenReturn(Map.of("stockDisponible", 1));
        when(restTemplate.getForObject(URI.create(INVENTARIO_URL + "/idVariante/12"), Map.class))
                .thenReturn(Map.of("stockDisponible", 20));

        List<AvisoCambio> avisos = carritoService.revalidar(1L);

        assertEquals(2, avisos.size());
        assertEquals(TipoAviso.SIN_STOCK, avisos.get(0).getTipo());
        assertEquals(1L, avisos.get(0).getIdItemCarrito());
        assertEquals(TipoAviso.STOCK_INSUFICIENTE, avisos.get(1).getTipo());
        assertEquals(1, avisos.get(1).getStockDisponible());
    }

    @Test
    void revalidarSinRegistroDeInventario() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(itemCarritoRepository.findByCarritoIdCarrito(1L)).thenReturn(List.of(bicicleta));
        when(restTemplate.getForObject(URI.create(INVENTARIO_URL + "/idVariante/10"), Map.class))
                .thenThrow(notFound());

        List<AvisoCambio> avisos = carritoService.revalidar(1L);

        assertEquals(TipoAviso.SIN_STOCK, avisos.get(0).getTipo());
    }

    @Test
    void validarCuponPorcentaje() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject(URI.create(CUPONES_URL + "/codigo/RIDER10"), Map.class))
                .thenReturn(cupon("PORCENTAJE", 10, true, "2099-12-31", 0, 100));

        ResultadoValidacionCupon resultado = carritoService.validarCupon(1L, "RIDER10");

        assertTrue(resultado.getValido());
        assertEquals(97998, resultado.getMontoDescuento());
    }

    @Test
    void validarCuponMontoFijo() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject(URI.create(CUPONES_URL + "/codigo/BIENVENIDA5000"), Map.class))
                .thenReturn(cupon("MONTO_FIJO", 5000, true, "2099-12-31", 0, 0));

        ResultadoValidacionCupon resultado = carritoService.validarCupon(1L, "BIENVENIDA5000");

        assertTrue(resultado.getValido());
        assertEquals(5000, resultado.getMontoDescuento());
    }

    @Test
    void validarCuponNoExistente() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject(URI.create(CUPONES_URL + "/codigo/NOEXISTE"), Map.class))
                .thenThrow(notFound());

        ResultadoValidacionCupon resultado = carritoService.validarCupon(1L, "NOEXISTE");

        assertFalse(resultado.getValido());
        assertEquals("El cupón no existe", resultado.getMensaje());
    }

    @Test
    void validarCuponInactivo() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject(URI.create(CUPONES_URL + "/codigo/RIDER10"), Map.class))
                .thenReturn(cupon("PORCENTAJE", 10, false, "2099-12-31", 0, 100));

        ResultadoValidacionCupon resultado = carritoService.validarCupon(1L, "RIDER10");

        assertFalse(resultado.getValido());
        assertEquals("El cupón no está activo", resultado.getMensaje());
    }

    @Test
    void validarCuponVencido() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject(URI.create(CUPONES_URL + "/codigo/RIDER10"), Map.class))
                .thenReturn(cupon("PORCENTAJE", 10, true, "2020-01-31", 0, 100));

        ResultadoValidacionCupon resultado = carritoService.validarCupon(1L, "RIDER10");

        assertFalse(resultado.getValido());
        assertEquals("El cupón está vencido", resultado.getMensaje());
    }

    @Test
    void validarCuponSinUsosDisponibles() {
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
        when(restTemplate.getForObject(URI.create(CUPONES_URL + "/codigo/RIDER10"), Map.class))
                .thenReturn(cupon("PORCENTAJE", 10, true, "2099-12-31", 100, 100));

        ResultadoValidacionCupon resultado = carritoService.validarCupon(1L, "RIDER10");

        assertFalse(resultado.getValido());
        assertEquals("El cupón alcanzó su límite de usos", resultado.getMensaje());
    }
}
