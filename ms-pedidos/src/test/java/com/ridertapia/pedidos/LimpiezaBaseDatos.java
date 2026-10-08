package com.ridertapia.pedidos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ridertapia.pedidos.repository.BoletaRepository;
import com.ridertapia.pedidos.repository.DatosVisitanteRepository;
import com.ridertapia.pedidos.repository.DetallePedidoRepository;
import com.ridertapia.pedidos.repository.FacturaRepository;
import com.ridertapia.pedidos.repository.HistorialEstadoPedidoRepository;
import com.ridertapia.pedidos.repository.NotificacionPedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

/**
 * Deja la base H2 vacía antes de cada test de integración.
 * Borra en orden (primero los registros que dependen de otros) para no romper las claves foráneas.
 */
@Component
public class LimpiezaBaseDatos {

    @Autowired
    private NotificacionPedidoRepository notificacionPedidoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private BoletaRepository boletaRepository;

    @Autowired
    private HistorialEstadoPedidoRepository historialEstadoPedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DatosVisitanteRepository datosVisitanteRepository;

    public void limpiar() {
        notificacionPedidoRepository.deleteAll();
        facturaRepository.deleteAll();
        boletaRepository.deleteAll();
        historialEstadoPedidoRepository.deleteAll();
        detallePedidoRepository.deleteAll();
        pedidoRepository.deleteAll();
        datosVisitanteRepository.deleteAll();
    }
}
