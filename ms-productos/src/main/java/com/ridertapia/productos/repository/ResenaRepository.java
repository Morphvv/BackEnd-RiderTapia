package com.ridertapia.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.productos.model.Resena;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByProductoIdProducto(Long idProducto);
}
