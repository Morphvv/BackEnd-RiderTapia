package com.ridertapia.productos.model;

import java.time.LocalDateTime;

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
@Table(name = "avisos_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvisoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAvisoStock;

    @Column(nullable = false, length = 100)
    private String correo;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(nullable = false)
    private Boolean notificado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
}
