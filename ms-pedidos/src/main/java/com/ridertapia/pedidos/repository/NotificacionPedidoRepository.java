package com.ridertapia.pedidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.pedidos.model.NotificacionPedido;

@Repository
public interface NotificacionPedidoRepository extends JpaRepository<NotificacionPedido, Long> {

    List<NotificacionPedido> findByPedidoIdPedido(Long idPedido);
}
