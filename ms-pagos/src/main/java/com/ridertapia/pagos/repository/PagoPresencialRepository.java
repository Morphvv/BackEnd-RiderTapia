package com.ridertapia.pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.pagos.model.PagoPresencial;

@Repository
public interface PagoPresencialRepository extends JpaRepository<PagoPresencial, Long> {

    List<PagoPresencial> findByIdPedido(Long idPedido);
}
