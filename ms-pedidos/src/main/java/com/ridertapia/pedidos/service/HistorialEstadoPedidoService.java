package com.ridertapia.pedidos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.pedidos.model.HistorialEstadoPedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.repository.HistorialEstadoPedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@Service
public class HistorialEstadoPedidoService {

    @Autowired
    private HistorialEstadoPedidoRepository historialEstadoPedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public HistorialEstadoPedido guardarHistorialEstadoPedido(HistorialEstadoPedido historialEstadoPedido) {
        historialEstadoPedido.setPedido(resolverPedido(historialEstadoPedido.getPedido()));
        return historialEstadoPedidoRepository.save(historialEstadoPedido);
    }

    public List<HistorialEstadoPedido> listarHistorialesEstadoPedido() {
        return historialEstadoPedidoRepository.findAll();
    }

    public Optional<HistorialEstadoPedido> findById(Long id) {
        return historialEstadoPedidoRepository.findById(id);
    }

    public List<HistorialEstadoPedido> findByPedido(Long idPedido) {
        return historialEstadoPedidoRepository.findByPedidoIdPedido(idPedido);
    }

    public HistorialEstadoPedido actualizarHistorialEstadoPedido(Long id, HistorialEstadoPedido datos) {
        HistorialEstadoPedido historialEstadoPedidoActual = historialEstadoPedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HistorialEstadoPedido no encontrado con id: " + id));
        historialEstadoPedidoActual.setEstadoAnterior(datos.getEstadoAnterior());
        historialEstadoPedidoActual.setEstadoNuevo(datos.getEstadoNuevo());
        historialEstadoPedidoActual.setFechaCambio(datos.getFechaCambio());
        historialEstadoPedidoActual.setIdEmpleado(datos.getIdEmpleado());
        historialEstadoPedidoActual.setPedido(resolverPedido(datos.getPedido()));
        return historialEstadoPedidoRepository.save(historialEstadoPedidoActual);
    }

    public void eliminarHistorialEstadoPedido(Long id) {
        historialEstadoPedidoRepository.deleteById(id);
    }

    private Pedido resolverPedido(Pedido pedido) {
        if (pedido == null || pedido.getIdPedido() == null) {
            throw new RuntimeException("Pedido no encontrado con id: null");
        }
        return pedidoRepository.findById(pedido.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + pedido.getIdPedido()));
    }
}
