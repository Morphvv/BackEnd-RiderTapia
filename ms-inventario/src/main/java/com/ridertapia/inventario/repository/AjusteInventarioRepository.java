package com.ridertapia.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.inventario.model.AjusteInventario;

@Repository
public interface AjusteInventarioRepository extends JpaRepository<AjusteInventario, Long> {

    List<AjusteInventario> findByItemInventarioIdItemInventario(Long idItemInventario);
}
