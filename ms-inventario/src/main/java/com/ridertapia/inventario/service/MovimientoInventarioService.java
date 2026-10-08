package com.ridertapia.inventario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.model.MovimientoInventario;
import com.ridertapia.inventario.repository.ItemInventarioRepository;
import com.ridertapia.inventario.repository.MovimientoInventarioRepository;

@Service
public class MovimientoInventarioService {

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    public MovimientoInventario guardarMovimientoInventario(MovimientoInventario movimientoInventario) {
        movimientoInventario.setItemInventario(resolverItemInventario(movimientoInventario.getItemInventario()));
        return movimientoInventarioRepository.save(movimientoInventario);
    }

    public List<MovimientoInventario> listarMovimientosInventario() {
        return movimientoInventarioRepository.findAll();
    }

    public Optional<MovimientoInventario> findById(Long id) {
        return movimientoInventarioRepository.findById(id);
    }

    public List<MovimientoInventario> findByItemInventario(Long idItemInventario) {
        return movimientoInventarioRepository.findByItemInventarioIdItemInventario(idItemInventario);
    }

    public MovimientoInventario actualizarMovimientoInventario(Long id, MovimientoInventario datos) {
        MovimientoInventario movimientoInventarioActual = movimientoInventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MovimientoInventario no encontrado con id: " + id));
        movimientoInventarioActual.setTipo(datos.getTipo());
        movimientoInventarioActual.setCantidad(datos.getCantidad());
        movimientoInventarioActual.setStockAnterior(datos.getStockAnterior());
        movimientoInventarioActual.setStockResultante(datos.getStockResultante());
        movimientoInventarioActual.setFecha(datos.getFecha());
        movimientoInventarioActual.setObservacion(datos.getObservacion());
        movimientoInventarioActual.setIdEmpleado(datos.getIdEmpleado());
        movimientoInventarioActual.setIdPedido(datos.getIdPedido());
        movimientoInventarioActual.setIdOrdenTrabajo(datos.getIdOrdenTrabajo());
        movimientoInventarioActual.setItemInventario(resolverItemInventario(datos.getItemInventario()));
        return movimientoInventarioRepository.save(movimientoInventarioActual);
    }

    public void eliminarMovimientoInventario(Long id) {
        movimientoInventarioRepository.deleteById(id);
    }

    private ItemInventario resolverItemInventario(ItemInventario itemInventario) {
        if (itemInventario == null || itemInventario.getIdItemInventario() == null) {
            throw new RuntimeException("ItemInventario no encontrado con id: null");
        }
        return itemInventarioRepository.findById(itemInventario.getIdItemInventario())
                .orElseThrow(() -> new RuntimeException("ItemInventario no encontrado con id: " + itemInventario.getIdItemInventario()));
    }
}
