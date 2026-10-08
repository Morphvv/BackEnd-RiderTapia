package com.ridertapia.inventario.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.repository.ItemInventarioRepository;

@Service
public class ItemInventarioService {

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.productos.variantes-url}")
    private String varianteUrl;

    public ItemInventario guardarItemInventario(ItemInventario itemInventario) {
        if (!existeVariante(itemInventario.getIdVariante())) {
            throw new RuntimeException("Variante no encontrada con id: " + itemInventario.getIdVariante());
        }
        return itemInventarioRepository.save(itemInventario);
    }

    public List<ItemInventario> listarItemsInventario() {
        return itemInventarioRepository.findAll();
    }

    public Optional<ItemInventario> findById(Long id) {
        return itemInventarioRepository.findById(id);
    }

    public ItemInventario findByIdVariante(Long idVariante) {
        return itemInventarioRepository.findByIdVariante(idVariante).orElse(null);
    }

    public ItemInventario actualizarItemInventario(Long id, ItemInventario datos) {
        ItemInventario itemInventarioActual = itemInventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ItemInventario no encontrado con id: " + id));
        itemInventarioActual.setIdVariante(datos.getIdVariante());
        itemInventarioActual.setStockDisponible(datos.getStockDisponible());
        itemInventarioActual.setStockReservado(datos.getStockReservado());
        itemInventarioActual.setStockMinimo(datos.getStockMinimo());
        itemInventarioActual.setCostoPromedio(datos.getCostoPromedio());
        itemInventarioActual.setFechaActualizacion(datos.getFechaActualizacion());
        itemInventarioActual.setIdProveedores(datos.getIdProveedores());
        return itemInventarioRepository.save(itemInventarioActual);
    }

    public void eliminarItemInventario(Long id) {
        itemInventarioRepository.deleteById(id);
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
}
