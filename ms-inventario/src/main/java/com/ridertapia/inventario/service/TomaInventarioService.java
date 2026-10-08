package com.ridertapia.inventario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.repository.TomaInventarioRepository;

@Service
public class TomaInventarioService {

    @Autowired
    private TomaInventarioRepository tomaInventarioRepository;

    public TomaInventario guardarTomaInventario(TomaInventario tomaInventario) {
        return tomaInventarioRepository.save(tomaInventario);
    }

    public List<TomaInventario> listarTomasInventario() {
        return tomaInventarioRepository.findAll();
    }

    public Optional<TomaInventario> findById(Long id) {
        return tomaInventarioRepository.findById(id);
    }

    public List<TomaInventario> findByIdEmpleado(Long idEmpleado) {
        return tomaInventarioRepository.findByIdEmpleado(idEmpleado);
    }

    public TomaInventario actualizarTomaInventario(Long id, TomaInventario datos) {
        TomaInventario tomaInventarioActual = tomaInventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TomaInventario no encontrada con id: " + id));
        tomaInventarioActual.setFechaInicio(datos.getFechaInicio());
        tomaInventarioActual.setFechaCierre(datos.getFechaCierre());
        tomaInventarioActual.setEstado(datos.getEstado());
        tomaInventarioActual.setIdEmpleado(datos.getIdEmpleado());
        return tomaInventarioRepository.save(tomaInventarioActual);
    }

    public void eliminarTomaInventario(Long id) {
        tomaInventarioRepository.deleteById(id);
    }
}
