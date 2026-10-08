package com.ridertapia.inventario.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tomas_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TomaInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTomaInventario;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = true)
    private LocalDateTime fechaCierre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoToma estado;

    @Column(nullable = false)
    private Long idEmpleado;
}
