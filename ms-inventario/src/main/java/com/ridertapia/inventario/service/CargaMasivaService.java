package com.ridertapia.inventario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.inventario.model.CargaMasiva;
import com.ridertapia.inventario.repository.CargaMasivaRepository;

@Service
public class CargaMasivaService {

    @Autowired
    private CargaMasivaRepository cargaMasivaRepository;

    public CargaMasiva guardarCargaMasiva(CargaMasiva cargaMasiva) {
        return cargaMasivaRepository.save(cargaMasiva);
    }

    public List<CargaMasiva> listarCargasMasivas() {
        return cargaMasivaRepository.findAll();
    }

    public Optional<CargaMasiva> findById(Long id) {
        return cargaMasivaRepository.findById(id);
    }

    public List<CargaMasiva> findByIdEmpleado(Long idEmpleado) {
        return cargaMasivaRepository.findByIdEmpleado(idEmpleado);
    }

    public CargaMasiva actualizarCargaMasiva(Long id, CargaMasiva datos) {
        CargaMasiva cargaMasivaActual = cargaMasivaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CargaMasiva no encontrada con id: " + id));
        cargaMasivaActual.setNombreArchivo(datos.getNombreArchivo());
        cargaMasivaActual.setFechaCarga(datos.getFechaCarga());
        cargaMasivaActual.setFilasProcesadas(datos.getFilasProcesadas());
        cargaMasivaActual.setFilasConError(datos.getFilasConError());
        cargaMasivaActual.setIdEmpleado(datos.getIdEmpleado());
        return cargaMasivaRepository.save(cargaMasivaActual);
    }

    public void eliminarCargaMasiva(Long id) {
        cargaMasivaRepository.deleteById(id);
    }
}
