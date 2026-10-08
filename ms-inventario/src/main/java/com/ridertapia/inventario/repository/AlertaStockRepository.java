package com.ridertapia.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.inventario.model.AlertaStock;

@Repository
public interface AlertaStockRepository extends JpaRepository<AlertaStock, Long> {

    List<AlertaStock> findByItemInventarioIdItemInventario(Long idItemInventario);
}
