package com.ridertapia.pedidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.pedidos.model.DatosVisitante;

@Repository
public interface DatosVisitanteRepository extends JpaRepository<DatosVisitante, Long> {

    List<DatosVisitante> findByCorreo(String correo);
}
