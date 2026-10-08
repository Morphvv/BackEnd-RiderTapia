package com.ridertapia.inventario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ridertapia.inventario.repository.AjusteInventarioRepository;
import com.ridertapia.inventario.repository.AlertaStockRepository;
import com.ridertapia.inventario.repository.CargaMasivaRepository;
import com.ridertapia.inventario.repository.DetalleConteoRepository;
import com.ridertapia.inventario.repository.ErrorCargaMasivaRepository;
import com.ridertapia.inventario.repository.IngresoMercanciaRepository;
import com.ridertapia.inventario.repository.ItemInventarioRepository;
import com.ridertapia.inventario.repository.MovimientoInventarioRepository;
import com.ridertapia.inventario.repository.TomaInventarioRepository;

/**
 * Deja la base H2 vacía antes de cada test de integración.
 * Borra en orden (primero los registros que dependen de otros) para no romper las claves foráneas.
 */
@Component
public class LimpiezaBaseDatos {

    @Autowired
    private ErrorCargaMasivaRepository errorCargaMasivaRepository;

    @Autowired
    private CargaMasivaRepository cargaMasivaRepository;

    @Autowired
    private DetalleConteoRepository detalleConteoRepository;

    @Autowired
    private AlertaStockRepository alertaStockRepository;

    @Autowired
    private IngresoMercanciaRepository ingresoMercanciaRepository;

    @Autowired
    private AjusteInventarioRepository ajusteInventarioRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @Autowired
    private TomaInventarioRepository tomaInventarioRepository;

    @Autowired
    private ItemInventarioRepository itemInventarioRepository;

    public void limpiar() {
        errorCargaMasivaRepository.deleteAll();
        cargaMasivaRepository.deleteAll();
        detalleConteoRepository.deleteAll();
        alertaStockRepository.deleteAll();
        ingresoMercanciaRepository.deleteAll();
        ajusteInventarioRepository.deleteAll();
        movimientoInventarioRepository.deleteAll();
        tomaInventarioRepository.deleteAll();
        itemInventarioRepository.deleteAll();
    }
}
