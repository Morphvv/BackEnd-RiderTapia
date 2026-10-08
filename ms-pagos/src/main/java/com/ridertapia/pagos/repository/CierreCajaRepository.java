package com.ridertapia.pagos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.pagos.model.CierreCaja;

@Repository
public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {

    List<CierreCaja> findByIdEmpleado(Long idEmpleado);
}
