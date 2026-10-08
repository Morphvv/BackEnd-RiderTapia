package com.ridertapia.pedidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.pedidos.model.HistorialEstadoPedido;

@Repository
public interface HistorialEstadoPedidoRepository extends JpaRepository<HistorialEstadoPedido, Long> {

    List<HistorialEstadoPedido> findByPedidoIdPedido(Long idPedido);
}
