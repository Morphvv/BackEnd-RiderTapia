package com.ridertapia.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.inventario.model.DetalleConteo;

@Repository
public interface DetalleConteoRepository extends JpaRepository<DetalleConteo, Long> {

    List<DetalleConteo> findByTomaInventarioIdTomaInventario(Long idTomaInventario);
}
