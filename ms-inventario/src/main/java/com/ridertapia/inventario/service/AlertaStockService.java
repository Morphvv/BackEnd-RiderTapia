package com.ridertapia.inventario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.inventario.model.AlertaStock;
import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.repository.AlertaStockRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;

@Service
public class AlertaStockService {

    @Autowired
    private AlertaStockRepository alertaStockRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    public AlertaStock guardarAlertaStock(AlertaStock alertaStock) {
        alertaStock.setItemInventario(resolverItemInventario(alertaStock.getItemInventario()));
        return alertaStockRepository.save(alertaStock);
    }

    public List<AlertaStock> listarAlertasStock() {
        return alertaStockRepository.findAll();
    }

    public Optional<AlertaStock> findById(Long id) {
        return alertaStockRepository.findById(id);
    }

    public List<AlertaStock> findByItemInventario(Long idItemInventario) {
        return alertaStockRepository.findByItemInventarioIdItemInventario(idItemInventario);
    }

    public AlertaStock actualizarAlertaStock(Long id, AlertaStock datos) {
        AlertaStock alertaStockActual = alertaStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AlertaStock no encontrada con id: " + id));
        alertaStockActual.setFechaGeneracion(datos.getFechaGeneracion());
        alertaStockActual.setStockAlMomento(datos.getStockAlMomento());
        alertaStockActual.setAtendida(datos.getAtendida());
        alertaStockActual.setItemInventario(resolverItemInventario(datos.getItemInventario()));
        return alertaStockRepository.save(alertaStockActual);
    }

    public void eliminarAlertaStock(Long id) {
        alertaStockRepository.deleteById(id);
    }

    private ItemInventario resolverItemInventario(ItemInventario itemInventario) {
        if (itemInventario == null || itemInventario.getIdItemInventario() == null) {
            throw new RuntimeException("ItemInventario no encontrado con id: null");
        }
        return itemInventarioRepository.findById(itemInventario.getIdItemInventario())
                .orElseThrow(() -> new RuntimeException("ItemInventario no encontrado con id: " + itemInventario.getIdItemInventario()));
    }
}
