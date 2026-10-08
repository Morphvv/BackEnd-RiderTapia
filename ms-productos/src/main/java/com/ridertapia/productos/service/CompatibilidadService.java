package com.ridertapia.productos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.Compatibilidad;
import com.ridertapia.productos.repository.CompatibilidadRepository;

@Service
public class CompatibilidadService {

    @Autowired
    private CompatibilidadRepository compatibilidadRepository;

    public Compatibilidad guardarCompatibilidad(Compatibilidad compatibilidad) {
        return compatibilidadRepository.save(compatibilidad);
    }

    public List<Compatibilidad> listarCompatibilidades() {
        return compatibilidadRepository.findAll();
    }

    public Optional<Compatibilidad> findById(Long id) {
        return compatibilidadRepository.findById(id);
    }

    public List<Compatibilidad> findByTipoBicicleta(String tipoBicicleta) {
        return compatibilidadRepository.findByTipoBicicleta(tipoBicicleta);
    }

    public Compatibilidad actualizarCompatibilidad(Long id, Compatibilidad datos) {
        Compatibilidad compatibilidadActual = compatibilidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compatibilidad no encontrada con id: " + id));
        compatibilidadActual.setTipoBicicleta(datos.getTipoBicicleta());
        compatibilidadActual.setAro(datos.getAro());
        compatibilidadActual.setVelocidades(datos.getVelocidades());
        return compatibilidadRepository.save(compatibilidadActual);
    }

    public void eliminarCompatibilidad(Long id) {
        compatibilidadRepository.deleteById(id);
    }
}
