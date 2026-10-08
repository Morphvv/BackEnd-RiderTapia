package com.ridertapia.productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.ImagenProducto;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.repository.ImagenProductoRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@Service
public class ImagenProductoService {

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public ImagenProducto guardarImagenProducto(ImagenProducto imagenProducto) {
        imagenProducto.setProducto(resolverProducto(imagenProducto.getProducto()));
        return imagenProductoRepository.save(imagenProducto);
    }

    public List<ImagenProducto> listarImagenesProducto() {
        return imagenProductoRepository.findAll();
    }

    public Optional<ImagenProducto> findById(Long id) {
        return imagenProductoRepository.findById(id);
    }

    public List<ImagenProducto> findByProducto(Long idProducto) {
        return imagenProductoRepository.findByProductoIdProducto(idProducto);
    }

    public ImagenProducto actualizarImagenProducto(Long id, ImagenProducto datos) {
        ImagenProducto imagenProductoActual = imagenProductoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ImagenProducto no encontrada con id: " + id));
        imagenProductoActual.setUrl(datos.getUrl());
        imagenProductoActual.setOrden(datos.getOrden());
        imagenProductoActual.setEsPrincipal(datos.getEsPrincipal());
        imagenProductoActual.setProducto(resolverProducto(datos.getProducto()));
        return imagenProductoRepository.save(imagenProductoActual);
    }

    public void eliminarImagenProducto(Long id) {
        imagenProductoRepository.deleteById(id);
    }

    private Producto resolverProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new RuntimeException("Producto no encontrado con id: null");
        }
        return productoRepository.findById(producto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + producto.getIdProducto()));
    }
}
