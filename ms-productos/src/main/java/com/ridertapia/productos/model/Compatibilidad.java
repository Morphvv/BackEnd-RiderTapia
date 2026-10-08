package com.ridertapia.productos.model;


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
@Table(name = "compatibilidades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Compatibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompatibilidad;

    @Column(nullable = false, length = 30)
    private String tipoBicicleta;

    @Column(nullable = true, length = 10)
    private String aro;

    @Column(nullable = true, length = 10)
    private String velocidades;
}
