package com.ridertapia.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "errores_carga_masiva")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorCargaMasiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idErrorCargaMasiva;

    @Column(nullable = false)
    private Integer fila;

    @Column(nullable = false, length = 255)
    private String mensaje;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_carga_masiva", nullable = false)
    private CargaMasiva cargaMasiva;
}
