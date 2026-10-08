package com.ridertapia.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.productos.model.Oferta;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, Long> {

    List<Oferta> findByProductoIdProducto(Long idProducto);
}
