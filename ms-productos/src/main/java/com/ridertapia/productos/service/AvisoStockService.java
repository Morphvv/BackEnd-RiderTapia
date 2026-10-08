package com.ridertapia.productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.AvisoStock;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.repository.AvisoStockRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@Service
public class AvisoStockService {

    @Autowired
    private AvisoStockRepository avisoStockRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public AvisoStock guardarAvisoStock(AvisoStock avisoStock) {
        avisoStock.setProducto(resolverProducto(avisoStock.getProducto()));
        return avisoStockRepository.save(avisoStock);
    }

    public List<AvisoStock> listarAvisosStock() {
        return avisoStockRepository.findAll();
    }

    public Optional<AvisoStock> findById(Long id) {
        return avisoStockRepository.findById(id);
    }

    public List<AvisoStock> findByProducto(Long idProducto) {
        return avisoStockRepository.findByProductoIdProducto(idProducto);
    }

    public AvisoStock actualizarAvisoStock(Long id, AvisoStock datos) {
        AvisoStock avisoStockActual = avisoStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AvisoStock no encontrado con id: " + id));
        avisoStockActual.setCorreo(datos.getCorreo());
        avisoStockActual.setFechaSolicitud(datos.getFechaSolicitud());
        avisoStockActual.setNotificado(datos.getNotificado());
        avisoStockActual.setProducto(resolverProducto(datos.getProducto()));
        return avisoStockRepository.save(avisoStockActual);
    }

    public void eliminarAvisoStock(Long id) {
        avisoStockRepository.deleteById(id);
    }

    private Producto resolverProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new RuntimeException("Producto no encontrado con id: null");
        }
        return productoRepository.findById(producto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + producto.getIdProducto()));
    }
}
