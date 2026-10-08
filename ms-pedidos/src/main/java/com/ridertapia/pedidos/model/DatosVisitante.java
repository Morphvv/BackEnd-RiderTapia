package com.ridertapia.pedidos.model;

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
@Table(name = "datos_visitantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatosVisitante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDatosVisitante;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String correo;

    @Column(nullable = true, length = 20)
    private String telefono;
}
