package com.ridertapia.pedidos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.pedidos.model.DatosVisitante;
import com.ridertapia.pedidos.repository.DatosVisitanteRepository;

@Service
public class DatosVisitanteService {

    @Autowired
    private DatosVisitanteRepository datosVisitanteRepository;

    public DatosVisitante guardarDatosVisitante(DatosVisitante datosVisitante) {
        return datosVisitanteRepository.save(datosVisitante);
    }

    public List<DatosVisitante> listarDatosVisitantes() {
        return datosVisitanteRepository.findAll();
    }

    public Optional<DatosVisitante> findById(Long id) {
        return datosVisitanteRepository.findById(id);
    }

    public List<DatosVisitante> findByCorreo(String correo) {
        return datosVisitanteRepository.findByCorreo(correo);
    }

    public DatosVisitante actualizarDatosVisitante(Long id, DatosVisitante datos) {
        DatosVisitante datosVisitanteActual = datosVisitanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DatosVisitante no encontrado con id: " + id));
        datosVisitanteActual.setNombre(datos.getNombre());
        datosVisitanteActual.setCorreo(datos.getCorreo());
        datosVisitanteActual.setTelefono(datos.getTelefono());
        return datosVisitanteRepository.save(datosVisitanteActual);
    }

    public void eliminarDatosVisitante(Long id) {
        datosVisitanteRepository.deleteById(id);
    }
}
