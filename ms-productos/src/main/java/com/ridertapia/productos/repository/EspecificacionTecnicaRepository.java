package com.ridertapia.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.productos.model.EspecificacionTecnica;

@Repository
public interface EspecificacionTecnicaRepository extends JpaRepository<EspecificacionTecnica, Long> {

    List<EspecificacionTecnica> findByProductoIdProducto(Long idProducto);
}
