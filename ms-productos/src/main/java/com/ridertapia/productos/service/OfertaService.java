package com.ridertapia.productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.Oferta;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.repository.OfertaRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@Service
public class OfertaService {

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public Oferta guardarOferta(Oferta oferta) {
        oferta.setProducto(resolverProducto(oferta.getProducto()));
        return ofertaRepository.save(oferta);
    }

    public List<Oferta> listarOfertas() {
        return ofertaRepository.findAll();
    }

    public Optional<Oferta> findById(Long id) {
        return ofertaRepository.findById(id);
    }

    public List<Oferta> findByProducto(Long idProducto) {
        return ofertaRepository.findByProductoIdProducto(idProducto);
    }

    public Oferta actualizarOferta(Long id, Oferta datos) {
        Oferta ofertaActual = ofertaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oferta no encontrada con id: " + id));
        ofertaActual.setPrecioOferta(datos.getPrecioOferta());
        ofertaActual.setFechaInicio(datos.getFechaInicio());
        ofertaActual.setFechaTermino(datos.getFechaTermino());
        ofertaActual.setProducto(resolverProducto(datos.getProducto()));
        return ofertaRepository.save(ofertaActual);
    }

    public void eliminarOferta(Long id) {
        ofertaRepository.deleteById(id);
    }

    private Producto resolverProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new RuntimeException("Producto no encontrado con id: null");
        }
        return productoRepository.findById(producto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + producto.getIdProducto()));
    }
}
