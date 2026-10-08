package com.ridertapia.productos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.productos.model.Cupon;

@Repository
public interface CuponRepository extends JpaRepository<Cupon, Long> {

    Optional<Cupon> findByCodigo(String codigo);
}
