package com.ridertapia.productos.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Compatibilidad;
import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.CompatibilidadRepository;
import com.ridertapia.productos.repository.MarcaRepository;
import com.ridertapia.productos.repository.ProductoRepository;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private CompatibilidadRepository compatibilidadRepository;

    public Producto guardarProducto(Producto producto) {
        producto.setCategoria(resolverCategoria(producto.getCategoria()));
        producto.setMarca(resolverMarca(producto.getMarca()));
        producto.setCompatibilidades(resolverCompatibilidades(producto.getCompatibilidades()));
        return productoRepository.save(producto);
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> findByCategoria(Long idCategoria) {
        return productoRepository.findByCategoriaIdCategoria(idCategoria);
    }

    public Producto actualizarProducto(Long id, Producto datos) {
        Producto productoActual = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        productoActual.setNombre(datos.getNombre());
        productoActual.setDescripcion(datos.getDescripcion());
        productoActual.setPrecioNormal(datos.getPrecioNormal());
        productoActual.setEstado(datos.getEstado());
        productoActual.setDestacado(datos.getDestacado());
        productoActual.setOrdenDestacado(datos.getOrdenDestacado());
        productoActual.setLimitePorCompra(datos.getLimitePorCompra());
        productoActual.setFechaCreacion(datos.getFechaCreacion());
        productoActual.setCategoria(resolverCategoria(datos.getCategoria()));
        productoActual.setMarca(resolverMarca(datos.getMarca()));
        productoActual.setCompatibilidades(resolverCompatibilidades(datos.getCompatibilidades()));
        return productoRepository.save(productoActual);
    }

    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }

    private Categoria resolverCategoria(Categoria categoria) {
        if (categoria == null || categoria.getIdCategoria() == null) {
            throw new RuntimeException("Categoria no encontrada con id: null");
        }
        return categoriaRepository.findById(categoria.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + categoria.getIdCategoria()));
    }

    private Marca resolverMarca(Marca marca) {
        if (marca == null || marca.getIdMarca() == null) {
            throw new RuntimeException("Marca no encontrada con id: null");
        }
        return marcaRepository.findById(marca.getIdMarca())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + marca.getIdMarca()));
    }

    private List<Compatibilidad> resolverCompatibilidades(List<Compatibilidad> lista) {
        if (lista == null || lista.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = lista.stream().map(Compatibilidad::getIdCompatibilidad).toList();
        List<Compatibilidad> encontrados = compatibilidadRepository.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            throw new RuntimeException("Compatibilidad no encontrada en: " + ids);
        }
        return new ArrayList<>(encontrados);
    }
}
