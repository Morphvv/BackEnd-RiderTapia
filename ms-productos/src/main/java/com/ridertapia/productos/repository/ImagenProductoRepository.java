package com.ridertapia.productos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ridertapia.productos.model.ImagenProducto;

@Repository
public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {

    List<ImagenProducto> findByProductoIdProducto(Long idProducto);
}
