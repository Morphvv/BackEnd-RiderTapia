package com.ridertapia.productos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.productos.model.VarianteProducto;

@Repository
public interface VarianteProductoRepository extends JpaRepository<VarianteProducto, Long> {

    Optional<VarianteProducto> findBySku(String sku);
}
