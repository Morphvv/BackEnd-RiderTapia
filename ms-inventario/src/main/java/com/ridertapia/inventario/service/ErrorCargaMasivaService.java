package com.ridertapia.inventario.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.inventario.model.CargaMasiva;
import com.ridertapia.inventario.model.ErrorCargaMasiva;
import com.ridertapia.inventario.repository.CargaMasivaRepository;
import com.ridertapia.inventario.repository.ErrorCargaMasivaRepository;

@Service
public class ErrorCargaMasivaService {

    @Autowired
    private ErrorCargaMasivaRepository errorCargaMasivaRepository;

    @Autowired
    private CargaMasivaRepository cargaMasivaRepository;

    public ErrorCargaMasiva guardarErrorCargaMasiva(ErrorCargaMasiva errorCargaMasiva) {
        errorCargaMasiva.setCargaMasiva(resolverCargaMasiva(errorCargaMasiva.getCargaMasiva()));
        return errorCargaMasivaRepository.save(errorCargaMasiva);
    }

    public List<ErrorCargaMasiva> listarErroresCargaMasiva() {
        return errorCargaMasivaRepository.findAll();
    }

    public Optional<ErrorCargaMasiva> findById(Long id) {
        return errorCargaMasivaRepository.findById(id);
    }

    public List<ErrorCargaMasiva> findByCargaMasiva(Long idCargaMasiva) {
        return errorCargaMasivaRepository.findByCargaMasivaIdCargaMasiva(idCargaMasiva);
    }

    public ErrorCargaMasiva actualizarErrorCargaMasiva(Long id, ErrorCargaMasiva datos) {
        ErrorCargaMasiva errorCargaMasivaActual = errorCargaMasivaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ErrorCargaMasiva no encontrado con id: " + id));
        errorCargaMasivaActual.setFila(datos.getFila());
        errorCargaMasivaActual.setMensaje(datos.getMensaje());
        errorCargaMasivaActual.setCargaMasiva(resolverCargaMasiva(datos.getCargaMasiva()));
        return errorCargaMasivaRepository.save(errorCargaMasivaActual);
    }

    public void eliminarErrorCargaMasiva(Long id) {
        errorCargaMasivaRepository.deleteById(id);
    }

    private CargaMasiva resolverCargaMasiva(CargaMasiva cargaMasiva) {
        if (cargaMasiva == null || cargaMasiva.getIdCargaMasiva() == null) {
            throw new RuntimeException("CargaMasiva no encontrada con id: null");
        }
        return cargaMasivaRepository.findById(cargaMasiva.getIdCargaMasiva())
                .orElseThrow(() -> new RuntimeException("CargaMasiva no encontrada con id: " + cargaMasiva.getIdCargaMasiva()));
    }
}
