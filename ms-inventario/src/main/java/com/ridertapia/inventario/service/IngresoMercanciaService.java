package com.ridertapia.inventario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.inventario.model.IngresoMercancia;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.repository.IngresoMercanciaRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;

@Service
public class IngresoMercanciaService {

    @Autowired
    private IngresoMercanciaRepository ingresoMercanciaRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    public IngresoMercancia guardarIngresoMercancia(IngresoMercancia ingresoMercancia) {
        ingresoMercancia.setItemInventario(resolverItemInventario(ingresoMercancia.getItemInventario()));
        return ingresoMercanciaRepository.save(ingresoMercancia);
    }

    public List<IngresoMercancia> listarIngresosMercancia() {
        return ingresoMercanciaRepository.findAll();
    }

    public Optional<IngresoMercancia> findById(Long id) {
        return ingresoMercanciaRepository.findById(id);
    }

    public List<IngresoMercancia> findByItemInventario(Long idItemInventario) {
        return ingresoMercanciaRepository.findByItemInventarioIdItemInventario(idItemInventario);
    }

    public IngresoMercancia actualizarIngresoMercancia(Long id, IngresoMercancia datos) {
        IngresoMercancia ingresoMercanciaActual = ingresoMercanciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IngresoMercancia no encontrado con id: " + id));
        ingresoMercanciaActual.setTipo(datos.getTipo());
        ingresoMercanciaActual.setCantidad(datos.getCantidad());
        ingresoMercanciaActual.setStockAnterior(datos.getStockAnterior());
        ingresoMercanciaActual.setStockResultante(datos.getStockResultante());
        ingresoMercanciaActual.setFecha(datos.getFecha());
        ingresoMercanciaActual.setObservacion(datos.getObservacion());
        ingresoMercanciaActual.setIdEmpleado(datos.getIdEmpleado());
        ingresoMercanciaActual.setIdPedido(datos.getIdPedido());
        ingresoMercanciaActual.setIdOrdenTrabajo(datos.getIdOrdenTrabajo());
        ingresoMercanciaActual.setItemInventario(resolverItemInventario(datos.getItemInventario()));
        ingresoMercanciaActual.setCostoUnitario(datos.getCostoUnitario());
        ingresoMercanciaActual.setNumeroDocumento(datos.getNumeroDocumento());
        ingresoMercanciaActual.setIdProveedor(datos.getIdProveedor());
        return ingresoMercanciaRepository.save(ingresoMercanciaActual);
    }

    public void eliminarIngresoMercancia(Long id) {
        ingresoMercanciaRepository.deleteById(id);
    }

    private ItemInventario resolverItemInventario(ItemInventario itemInventario) {
        if (itemInventario == null || itemInventario.getIdItemInventario() == null) {
            throw new RuntimeException("ItemInventario no encontrado con id: null");
        }
        return itemInventarioRepository.findById(itemInventario.getIdItemInventario())
                .orElseThrow(() -> new RuntimeException("ItemInventario no encontrado con id: " + itemInventario.getIdItemInventario()));
    }
}
