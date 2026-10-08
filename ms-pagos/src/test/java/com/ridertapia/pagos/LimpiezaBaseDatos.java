package com.ridertapia.pagos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ridertapia.pagos.repository.CierreCajaRepository;
import com.ridertapia.pagos.repository.PagoPresencialRepository;
import com.ridertapia.pagos.repository.PagoTransferenciaRepository;
import com.ridertapia.pagos.repository.PagoWebPayRepository;

/**
 * Deja la base H2 vacía antes de cada test de integración.
 * Borra en orden (primero los registros que dependen de otros) para no romper las claves foráneas.
 */
@Component
public class LimpiezaBaseDatos {

    @Autowired
    private PagoWebPayRepository pagoWebPayRepository;

    @Autowired
    private PagoTransferenciaRepository pagoTransferenciaRepository;

    @Autowired
    private PagoPresencialRepository pagoPresencialRepository;

    @Autowired
    private CierreCajaRepository cierreCajaRepository;

    public void limpiar() {
        pagoWebPayRepository.deleteAll();
        pagoTransferenciaRepository.deleteAll();
        pagoPresencialRepository.deleteAll();
        cierreCajaRepository.deleteAll();
    }
}
