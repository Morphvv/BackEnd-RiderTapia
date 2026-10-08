package com.ridertapia.carrito.service;

import java.net.URI;
import java.util.Map;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.carrito.model.Carrito;
import com.ridertapia.carrito.model.ItemCarrito;
import com.ridertapia.carrito.repository.CarritoRepository;
import com.ridertapia.carrito.repository.ItemCarritoRepository;

@Service
public class ItemCarritoService {

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.productos.variantes-url}")
    private String varianteUrl;

    @Value("${ms.inventario.url}")
    private String inventarioUrl;

    public ItemCarrito guardarItemCarrito(ItemCarrito itemCarrito) {
        if (!existeVariante(itemCarrito.getIdVariante())) {
            throw new RuntimeException("Variante no encontrada con id: " + itemCarrito.getIdVariante());
        }
        itemCarrito.setCarrito(resolverCarrito(itemCarrito.getCarrito()));
        validarStock(itemCarrito.getIdVariante(), itemCarrito.getCantidad());
        return itemCarritoRepository.save(itemCarrito);
    }

    public List<ItemCarrito> listarItemsCarrito() {
        return itemCarritoRepository.findAll();
    }

    public Optional<ItemCarrito> findById(Long id) {
        return itemCarritoRepository.findById(id);
    }

    public List<ItemCarrito> findByCarrito(Long idCarrito) {
        return itemCarritoRepository.findByCarritoIdCarrito(idCarrito);
    }

    public ItemCarrito actualizarItemCarrito(Long id, ItemCarrito datos) {
        ItemCarrito itemCarritoActual = itemCarritoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ItemCarrito no encontrado con id: " + id));
        itemCarritoActual.setIdVariante(datos.getIdVariante());
        itemCarritoActual.setCantidad(datos.getCantidad());
        itemCarritoActual.setPrecioUnitario(datos.getPrecioUnitario());
        itemCarritoActual.setFechaAgregado(datos.getFechaAgregado());
        itemCarritoActual.setCarrito(resolverCarrito(datos.getCarrito()));
        validarStock(itemCarritoActual.getIdVariante(), itemCarritoActual.getCantidad());
        return itemCarritoRepository.save(itemCarritoActual);
    }

    public void eliminarItemCarrito(Long id) {
        itemCarritoRepository.deleteById(id);
    }

    // Consulta a ms-productos: GET /api/v1/variantes/{id}
    private boolean existeVariante(Long id) {
        try {
            restTemplate.getForEntity(URI.create(varianteUrl + "/" + id), String.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    private Carrito resolverCarrito(Carrito carrito) {
        if (carrito == null || carrito.getIdCarrito() == null) {
            throw new RuntimeException("Carrito no encontrado con id: null");
        }
        return carritoRepository.findById(carrito.getIdCarrito())
                .orElseThrow(() -> new RuntimeException("Carrito no encontrado con id: " + carrito.getIdCarrito()));
    }

    // Consulta a ms-inventario: GET /api/v1/items-inventario/idVariante/{idVariante} (HU-19 y HU-20)
    private void validarStock(Long idVariante, Integer cantidad) {
        int disponible;
        try {
            Map<?, ?> item = restTemplate.getForObject(URI.create(inventarioUrl + "/idVariante/" + idVariante), Map.class);
            disponible = (item == null || item.get("stockDisponible") == null)
                    ? 0
                    : ((Number) item.get("stockDisponible")).intValue();
        } catch (HttpClientErrorException.NotFound e) {
            disponible = 0;
        }
        if (cantidad > disponible) {
            throw new IllegalStateException("Stock insuficiente para la variante con id: " + idVariante);
        }
    }
}
