package com.ridertapia.pedidos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.pedidos.model.NotificacionPedido;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.repository.NotificacionPedidoRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@Service
public class NotificacionPedidoService {

    @Autowired
    private NotificacionPedidoRepository notificacionPedidoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public NotificacionPedido guardarNotificacionPedido(NotificacionPedido notificacionPedido) {
        notificacionPedido.setPedido(resolverPedido(notificacionPedido.getPedido()));
        return notificacionPedidoRepository.save(notificacionPedido);
    }

    public List<NotificacionPedido> listarNotificacionesPedido() {
        return notificacionPedidoRepository.findAll();
    }

    public Optional<NotificacionPedido> findById(Long id) {
        return notificacionPedidoRepository.findById(id);
    }

    public List<NotificacionPedido> findByPedido(Long idPedido) {
        return notificacionPedidoRepository.findByPedidoIdPedido(idPedido);
    }

    public NotificacionPedido actualizarNotificacionPedido(Long id, NotificacionPedido datos) {
        NotificacionPedido notificacionPedidoActual = notificacionPedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("NotificacionPedido no encontrada con id: " + id));
        notificacionPedidoActual.setTipo(datos.getTipo());
        notificacionPedidoActual.setDestinatario(datos.getDestinatario());
        notificacionPedidoActual.setFechaEnvio(datos.getFechaEnvio());
        notificacionPedidoActual.setEnviada(datos.getEnviada());
        notificacionPedidoActual.setPedido(resolverPedido(datos.getPedido()));
        return notificacionPedidoRepository.save(notificacionPedidoActual);
    }

    public void eliminarNotificacionPedido(Long id) {
        notificacionPedidoRepository.deleteById(id);
    }

    private Pedido resolverPedido(Pedido pedido) {
        if (pedido == null || pedido.getIdPedido() == null) {
            throw new RuntimeException("Pedido no encontrado con id: null");
        }
        return pedidoRepository.findById(pedido.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + pedido.getIdPedido()));
    }
}
