package com.ridertapia.productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.Resena;
import com.ridertapia.productos.repository.ProductoRepository;
import com.ridertapia.productos.repository.ResenaRepository;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Resena guardarResena(Resena resena) {
        resena.setProducto(resolverProducto(resena.getProducto()));
        return resenaRepository.save(resena);
    }

    public List<Resena> listarResenas() {
        return resenaRepository.findAll();
    }

    public Optional<Resena> findById(Long id) {
        return resenaRepository.findById(id);
    }

    public List<Resena> findByProducto(Long idProducto) {
        return resenaRepository.findByProductoIdProducto(idProducto);
    }

    public Resena actualizarResena(Long id, Resena datos) {
        Resena resenaActual = resenaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resena no encontrada con id: " + id));
        resenaActual.setIdCliente(datos.getIdCliente());
        resenaActual.setCalificacion(datos.getCalificacion());
        resenaActual.setComentario(datos.getComentario());
        resenaActual.setFecha(datos.getFecha());
        resenaActual.setVisible(datos.getVisible());
        resenaActual.setRespuestaTienda(datos.getRespuestaTienda());
        resenaActual.setProducto(resolverProducto(datos.getProducto()));
        return resenaRepository.save(resenaActual);
    }

    public void eliminarResena(Long id) {
        resenaRepository.deleteById(id);
    }

    private Producto resolverProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new RuntimeException("Producto no encontrado con id: null");
        }
        return productoRepository.findById(producto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + producto.getIdProducto()));
    }
}
