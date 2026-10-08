package com.ridertapia.pedidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.pedidos.model.Factura;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByPedidoIdPedido(Long idPedido);
}
