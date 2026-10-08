package com.ridertapia.pagos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.pagos.model.CierreCaja;
import com.ridertapia.pagos.repository.CierreCajaRepository;

@Service
public class CierreCajaService {

    @Autowired
    private CierreCajaRepository cierreCajaRepository;

    public CierreCaja guardarCierreCaja(CierreCaja cierreCaja) {
        return cierreCajaRepository.save(cierreCaja);
    }

    public List<CierreCaja> listarCierresCaja() {
        return cierreCajaRepository.findAll();
    }

    public Optional<CierreCaja> findById(Long id) {
        return cierreCajaRepository.findById(id);
    }

    public List<CierreCaja> findByIdEmpleado(Long idEmpleado) {
        return cierreCajaRepository.findByIdEmpleado(idEmpleado);
    }

    public CierreCaja actualizarCierreCaja(Long id, CierreCaja datos) {
        CierreCaja cierreCajaActual = cierreCajaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CierreCaja no encontrado con id: " + id));
        cierreCajaActual.setFecha(datos.getFecha());
        cierreCajaActual.setTotalEfectivo(datos.getTotalEfectivo());
        cierreCajaActual.setTotalDebito(datos.getTotalDebito());
        cierreCajaActual.setTotalCredito(datos.getTotalCredito());
        cierreCajaActual.setTotalTransferencia(datos.getTotalTransferencia());
        cierreCajaActual.setEfectivoContado(datos.getEfectivoContado());
        cierreCajaActual.setDiferencia(datos.getDiferencia());
        cierreCajaActual.setIdEmpleado(datos.getIdEmpleado());
        return cierreCajaRepository.save(cierreCajaActual);
    }

    public void eliminarCierreCaja(Long id) {
        cierreCajaRepository.deleteById(id);
    }
}
