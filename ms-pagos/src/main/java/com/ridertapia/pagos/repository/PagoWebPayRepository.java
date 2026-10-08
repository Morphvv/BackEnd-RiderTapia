package com.ridertapia.pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.pagos.model.PagoWebPay;

@Repository
public interface PagoWebPayRepository extends JpaRepository<PagoWebPay, Long> {

    List<PagoWebPay> findByIdPedido(Long idPedido);
}
