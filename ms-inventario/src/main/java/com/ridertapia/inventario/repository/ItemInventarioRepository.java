package com.ridertapia.inventario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.inventario.model.ItemInventario;

@Repository
public interface ItemInventarioRepository extends JpaRepository<ItemInventario, Long> {

    Optional<ItemInventario> findByIdVariante(Long idVariante);
}
