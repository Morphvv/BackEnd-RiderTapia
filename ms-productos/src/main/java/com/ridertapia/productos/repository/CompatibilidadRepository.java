package com.ridertapia.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.productos.model.Compatibilidad;

@Repository
public interface CompatibilidadRepository extends JpaRepository<Compatibilidad, Long> {

    List<Compatibilidad> findByTipoBicicleta(String tipoBicicleta);
}
