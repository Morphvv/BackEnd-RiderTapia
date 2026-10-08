package com.ridertapia.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.inventario.model.IngresoMercancia;

@Repository
public interface IngresoMercanciaRepository extends JpaRepository<IngresoMercancia, Long> {

    List<IngresoMercancia> findByItemInventarioIdItemInventario(Long idItemInventario);
}
