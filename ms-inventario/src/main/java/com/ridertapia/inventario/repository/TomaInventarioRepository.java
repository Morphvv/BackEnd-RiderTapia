package com.ridertapia.inventario.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.inventario.model.TomaInventario;

@Repository
public interface TomaInventarioRepository extends JpaRepository<TomaInventario, Long> {

    List<TomaInventario> findByIdEmpleado(Long idEmpleado);
}
