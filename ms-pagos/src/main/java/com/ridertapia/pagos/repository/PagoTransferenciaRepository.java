package com.ridertapia.pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.pagos.model.PagoTransferencia;

@Repository
public interface PagoTransferenciaRepository extends JpaRepository<PagoTransferencia, Long> {

    List<PagoTransferencia> findByIdPedido(Long idPedido);
}
