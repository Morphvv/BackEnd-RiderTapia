package com.ridertapia.carrito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ridertapia.carrito.repository.CarritoRepository;
import com.ridertapia.carrito.repository.ItemCarritoRepository;

/**
 * Deja la base H2 vacía antes de cada test de integración.
 * Borra en orden (primero los registros que dependen de otros) para no romper las claves foráneas.
 */
@Component
public class LimpiezaBaseDatos {

    @Autowired
    private ItemCarritoRepository itemCarritoRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    public void limpiar() {
        itemCarritoRepository.deleteAll();
        carritoRepository.deleteAll();
    }
}
