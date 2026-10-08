package com.ridertapia.productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.repository.CategoriaRepository;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Categoria guardarCategoria(Categoria categoria) {
        categoria.setCategoriaPadre(resolverCategoriaPadre(categoria.getCategoriaPadre()));
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> findById(Long id) {
        return categoriaRepository.findById(id);
    }

    public Categoria findByNombre(String nombre) {
        return categoriaRepository.findByNombre(nombre).orElse(null);
    }

    public Categoria actualizarCategoria(Long id, Categoria datos) {
        Categoria categoriaActual = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + id));
        categoriaActual.setNombre(datos.getNombre());
        categoriaActual.setDescripcion(datos.getDescripcion());
        categoriaActual.setActiva(datos.getActiva());
        categoriaActual.setCategoriaPadre(resolverCategoriaPadre(datos.getCategoriaPadre()));
        return categoriaRepository.save(categoriaActual);
    }

    public void eliminarCategoria(Long id) {
        categoriaRepository.deleteById(id);
    }

    private Categoria resolverCategoriaPadre(Categoria categoria) {
        if (categoria == null || categoria.getIdCategoria() == null) {
            return null;
        }
        return categoriaRepository.findById(categoria.getIdCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada con id: " + categoria.getIdCategoria()));
    }
}
