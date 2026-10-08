package com.ridertapia.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.inventario.model.ErrorCargaMasiva;

@Repository
public interface ErrorCargaMasivaRepository extends JpaRepository<ErrorCargaMasiva, Long> {

    List<ErrorCargaMasiva> findByCargaMasivaIdCargaMasiva(Long idCargaMasiva);
}
