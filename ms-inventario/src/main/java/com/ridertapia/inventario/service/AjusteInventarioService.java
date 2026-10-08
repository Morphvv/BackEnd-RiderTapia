package com.ridertapia.inventario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.inventario.model.AjusteInventario;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.repository.AjusteInventarioRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;
import com.ridertapia.inventario.repository.TomaInventarioRepository;

@Service
public class AjusteInventarioService {

    @Autowired
    private AjusteInventarioRepository ajusteInventarioRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    @Autowired
    private TomaInventarioRepository tomaInventarioRepository;

    public AjusteInventario guardarAjusteInventario(AjusteInventario ajusteInventario) {
        ajusteInventario.setItemInventario(resolverItemInventario(ajusteInventario.getItemInventario()));
        ajusteInventario.setTomaInventario(resolverTomaInventario(ajusteInventario.getTomaInventario()));
        return ajusteInventarioRepository.save(ajusteInventario);
    }

    public List<AjusteInventario> listarAjustesInventario() {
        return ajusteInventarioRepository.findAll();
    }

    public Optional<AjusteInventario> findById(Long id) {
        return ajusteInventarioRepository.findById(id);
    }

    public List<AjusteInventario> findByItemInventario(Long idItemInventario) {
        return ajusteInventarioRepository.findByItemInventarioIdItemInventario(idItemInventario);
    }

    public AjusteInventario actualizarAjusteInventario(Long id, AjusteInventario datos) {
        AjusteInventario ajusteInventarioActual = ajusteInventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AjusteInventario no encontrado con id: " + id));
        ajusteInventarioActual.setTipo(datos.getTipo());
        ajusteInventarioActual.setCantidad(datos.getCantidad());
        ajusteInventarioActual.setStockAnterior(datos.getStockAnterior());
        ajusteInventarioActual.setStockResultante(datos.getStockResultante());
        ajusteInventarioActual.setFecha(datos.getFecha());
        ajusteInventarioActual.setObservacion(datos.getObservacion());
        ajusteInventarioActual.setIdEmpleado(datos.getIdEmpleado());
        ajusteInventarioActual.setIdPedido(datos.getIdPedido());
        ajusteInventarioActual.setIdOrdenTrabajo(datos.getIdOrdenTrabajo());
        ajusteInventarioActual.setItemInventario(resolverItemInventario(datos.getItemInventario()));
        ajusteInventarioActual.setMotivo(datos.getMotivo());
        ajusteInventarioActual.setTomaInventario(resolverTomaInventario(datos.getTomaInventario()));
        return ajusteInventarioRepository.save(ajusteInventarioActual);
    }

    public void eliminarAjusteInventario(Long id) {
        ajusteInventarioRepository.deleteById(id);
    }

    private ItemInventario resolverItemInventario(ItemInventario itemInventario) {
        if (itemInventario == null || itemInventario.getIdItemInventario() == null) {
            throw new RuntimeException("ItemInventario no encontrado con id: null");
        }
        return itemInventarioRepository.findById(itemInventario.getIdItemInventario())
                .orElseThrow(() -> new RuntimeException("ItemInventario no encontrado con id: " + itemInventario.getIdItemInventario()));
    }

    private TomaInventario resolverTomaInventario(TomaInventario tomaInventario) {
        if (tomaInventario == null || tomaInventario.getIdTomaInventario() == null) {
            return null;
        }
        return tomaInventarioRepository.findById(tomaInventario.getIdTomaInventario())
                .orElseThrow(() -> new RuntimeException("TomaInventario no encontrada con id: " + tomaInventario.getIdTomaInventario()));
    }
}
