package com.ridertapia.inventario.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimientos_inventario")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMovimientoInventario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Integer stockAnterior;

    @Column(nullable = false)
    private Integer stockResultante;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = true, length = 255)
    private String observacion;

    @Column(nullable = false)
    private Long idEmpleado;

    @Column(nullable = true)
    private Long idPedido;

    @Column(nullable = true)
    private Long idOrdenTrabajo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_item_inventario", nullable = false)
    private ItemInventario itemInventario;
}
