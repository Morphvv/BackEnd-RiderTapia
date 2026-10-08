package com.ridertapia.productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.model.VarianteProducto;
import com.ridertapia.productos.repository.ProductoRepository;
import com.ridertapia.productos.repository.VarianteProductoRepository;

@Service
public class VarianteProductoService {

    @Autowired
    private VarianteProductoRepository varianteProductoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public VarianteProducto guardarVarianteProducto(VarianteProducto varianteProducto) {
        varianteProducto.setProducto(resolverProducto(varianteProducto.getProducto()));
        return varianteProductoRepository.save(varianteProducto);
    }

    public List<VarianteProducto> listarVariantesProducto() {
        return varianteProductoRepository.findAll();
    }

    public Optional<VarianteProducto> findById(Long id) {
        return varianteProductoRepository.findById(id);
    }

    public VarianteProducto findBySku(String sku) {
        return varianteProductoRepository.findBySku(sku).orElse(null);
    }

    public VarianteProducto actualizarVarianteProducto(Long id, VarianteProducto datos) {
        VarianteProducto varianteProductoActual = varianteProductoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VarianteProducto no encontrada con id: " + id));
        varianteProductoActual.setSku(datos.getSku());
        varianteProductoActual.setTalla(datos.getTalla());
        varianteProductoActual.setColor(datos.getColor());
        varianteProductoActual.setAro(datos.getAro());
        varianteProductoActual.setProducto(resolverProducto(datos.getProducto()));
        return varianteProductoRepository.save(varianteProductoActual);
    }

    public void eliminarVarianteProducto(Long id) {
        varianteProductoRepository.deleteById(id);
    }

    private Producto resolverProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new RuntimeException("Producto no encontrado con id: null");
        }
        return productoRepository.findById(producto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + producto.getIdProducto()));
    }
}
