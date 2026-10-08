package com.ridertapia.inventario.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cargas_masivas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CargaMasiva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCargaMasiva;

    @Column(nullable = false, length = 150)
    private String nombreArchivo;

    @Column(nullable = false)
    private LocalDateTime fechaCarga;

    @Column(nullable = false)
    private Integer filasProcesadas;

    @Column(nullable = false)
    private Integer filasConError;

    @Column(nullable = false)
    private Long idEmpleado;
}
