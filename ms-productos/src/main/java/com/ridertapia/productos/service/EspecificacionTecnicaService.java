package com.ridertapia.productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.EspecificacionTecnica;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.repository.EspecificacionTecnicaRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@Service
public class EspecificacionTecnicaService {

    @Autowired
    private EspecificacionTecnicaRepository especificacionTecnicaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public EspecificacionTecnica guardarEspecificacionTecnica(EspecificacionTecnica especificacionTecnica) {
        especificacionTecnica.setProducto(resolverProducto(especificacionTecnica.getProducto()));
        return especificacionTecnicaRepository.save(especificacionTecnica);
    }

    public List<EspecificacionTecnica> listarEspecificacionesTecnicas() {
        return especificacionTecnicaRepository.findAll();
    }

    public Optional<EspecificacionTecnica> findById(Long id) {
        return especificacionTecnicaRepository.findById(id);
    }

    public List<EspecificacionTecnica> findByProducto(Long idProducto) {
        return especificacionTecnicaRepository.findByProductoIdProducto(idProducto);
    }

    public EspecificacionTecnica actualizarEspecificacionTecnica(Long id, EspecificacionTecnica datos) {
        EspecificacionTecnica especificacionTecnicaActual = especificacionTecnicaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("EspecificacionTecnica no encontrada con id: " + id));
        especificacionTecnicaActual.setNombre(datos.getNombre());
        especificacionTecnicaActual.setValor(datos.getValor());
        especificacionTecnicaActual.setProducto(resolverProducto(datos.getProducto()));
        return especificacionTecnicaRepository.save(especificacionTecnicaActual);
    }

    public void eliminarEspecificacionTecnica(Long id) {
        especificacionTecnicaRepository.deleteById(id);
    }

    private Producto resolverProducto(Producto producto) {
        if (producto == null || producto.getIdProducto() == null) {
            throw new RuntimeException("Producto no encontrado con id: null");
        }
        return productoRepository.findById(producto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + producto.getIdProducto()));
    }
}
