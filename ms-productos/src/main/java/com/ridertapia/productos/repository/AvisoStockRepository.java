package com.ridertapia.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.productos.model.AvisoStock;

@Repository
public interface AvisoStockRepository extends JpaRepository<AvisoStock, Long> {

    List<AvisoStock> findByProductoIdProducto(Long idProducto);
}
