package com.ridertapia.pedidos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.pedidos.model.DetallePedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.repository.DetallePedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@Service
public class DetallePedidoService {

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public DetallePedido guardarDetallePedido(DetallePedido detallePedido) {
        detallePedido.setPedido(resolverPedido(detallePedido.getPedido()));
        return detallePedidoRepository.save(detallePedido);
    }

    public List<DetallePedido> listarDetallesPedido() {
        return detallePedidoRepository.findAll();
    }

    public Optional<DetallePedido> findById(Long id) {
        return detallePedidoRepository.findById(id);
    }

    public List<DetallePedido> findByPedido(Long idPedido) {
        return detallePedidoRepository.findByPedidoIdPedido(idPedido);
    }

    public DetallePedido actualizarDetallePedido(Long id, DetallePedido datos) {
        DetallePedido detallePedidoActual = detallePedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DetallePedido no encontrado con id: " + id));
        detallePedidoActual.setIdVariante(datos.getIdVariante());
        detallePedidoActual.setCantidad(datos.getCantidad());
        detallePedidoActual.setPrecioUnitario(datos.getPrecioUnitario());
        detallePedidoActual.setSubtotal(datos.getSubtotal());
        detallePedidoActual.setPedido(resolverPedido(datos.getPedido()));
        return detallePedidoRepository.save(detallePedidoActual);
    }

    public void eliminarDetallePedido(Long id) {
        detallePedidoRepository.deleteById(id);
    }

    private Pedido resolverPedido(Pedido pedido) {
        if (pedido == null || pedido.getIdPedido() == null) {
            throw new RuntimeException("Pedido no encontrado con id: null");
        }
        return pedidoRepository.findById(pedido.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + pedido.getIdPedido()));
    }
}
