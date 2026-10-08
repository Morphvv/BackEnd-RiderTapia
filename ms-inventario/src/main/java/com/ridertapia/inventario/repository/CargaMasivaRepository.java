package com.ridertapia.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.inventario.model.CargaMasiva;

@Repository
public interface CargaMasivaRepository extends JpaRepository<CargaMasiva, Long> {

    List<CargaMasiva> findByIdEmpleado(Long idEmpleado);
}
