package com.ridertapia.carrito.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.carrito.model.Carrito;

@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    List<Carrito> findByRutCliente(String rutCliente);
}
