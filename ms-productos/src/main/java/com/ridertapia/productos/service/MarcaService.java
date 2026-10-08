package com.ridertapia.productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.repository.MarcaRepository;

@Service
public class MarcaService {

    @Autowired
    private MarcaRepository marcaRepository;

    public Marca guardarMarca(Marca marca) {
        return marcaRepository.save(marca);
    }

    public List<Marca> listarMarcas() {
        return marcaRepository.findAll();
    }

    public Optional<Marca> findById(Long id) {
        return marcaRepository.findById(id);
    }

    public Marca findByNombre(String nombre) {
        return marcaRepository.findByNombre(nombre).orElse(null);
    }

    public Marca actualizarMarca(Long id, Marca datos) {
        Marca marcaActual = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Marca no encontrada con id: " + id));
        marcaActual.setNombre(datos.getNombre());
        marcaActual.setActiva(datos.getActiva());
        return marcaRepository.save(marcaActual);
    }

    public void eliminarMarca(Long id) {
        marcaRepository.deleteById(id);
    }
}
