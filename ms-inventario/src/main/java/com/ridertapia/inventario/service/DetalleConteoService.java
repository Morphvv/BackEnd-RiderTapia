package com.ridertapia.inventario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.inventario.model.DetalleConteo;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.repository.DetalleConteoRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;
import com.ridertapia.inventario.repository.TomaInventarioRepository;

@Service
public class DetalleConteoService {

    @Autowired
    private DetalleConteoRepository detalleConteoRepository;

    @Autowired
    private TomaInventarioRepository tomaInventarioRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    public DetalleConteo guardarDetalleConteo(DetalleConteo detalleConteo) {
        detalleConteo.setTomaInventario(resolverTomaInventario(detalleConteo.getTomaInventario()));
        detalleConteo.setItemInventario(resolverItemInventario(detalleConteo.getItemInventario()));
        return detalleConteoRepository.save(detalleConteo);
    }

    public List<DetalleConteo> listarDetallesConteo() {
        return detalleConteoRepository.findAll();
    }

    public Optional<DetalleConteo> findById(Long id) {
        return detalleConteoRepository.findById(id);
    }

    public List<DetalleConteo> findByTomaInventario(Long idTomaInventario) {
        return detalleConteoRepository.findByTomaInventarioIdTomaInventario(idTomaInventario);
    }

    public DetalleConteo actualizarDetalleConteo(Long id, DetalleConteo datos) {
        DetalleConteo detalleConteoActual = detalleConteoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DetalleConteo no encontrado con id: " + id));
        detalleConteoActual.setCantidadSistema(datos.getCantidadSistema());
        detalleConteoActual.setCantidadContada(datos.getCantidadContada());
        detalleConteoActual.setDiferencia(datos.getDiferencia());
        detalleConteoActual.setTomaInventario(resolverTomaInventario(datos.getTomaInventario()));
        detalleConteoActual.setItemInventario(resolverItemInventario(datos.getItemInventario()));
        return detalleConteoRepository.save(detalleConteoActual);
    }

    public void eliminarDetalleConteo(Long id) {
        detalleConteoRepository.deleteById(id);
    }

    private TomaInventario resolverTomaInventario(TomaInventario tomaInventario) {
        if (tomaInventario == null || tomaInventario.getIdTomaInventario() == null) {
            throw new RuntimeException("TomaInventario no encontrada con id: null");
        }
        return tomaInventarioRepository.findById(tomaInventario.getIdTomaInventario())
                .orElseThrow(() -> new RuntimeException("TomaInventario no encontrada con id: " + tomaInventario.getIdTomaInventario()));
    }

    private ItemInventario resolverItemInventario(ItemInventario itemInventario) {
        if (itemInventario == null || itemInventario.getIdItemInventario() == null) {
            throw new RuntimeException("ItemInventario no encontrado con id: null");
        }
        return itemInventarioRepository.findById(itemInventario.getIdItemInventario())
                .orElseThrow(() -> new RuntimeException("ItemInventario no encontrado con id: " + itemInventario.getIdItemInventario()));
    }
}
