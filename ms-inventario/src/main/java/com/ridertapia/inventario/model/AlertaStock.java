package com.ridertapia.inventario.model;

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
@Table(name = "alertas_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAlertaStock;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(nullable = false)
    private Integer stockAlMomento;

    @Column(nullable = false)
    private Boolean atendida;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_item_inventario", nullable = false)
    private ItemInventario itemInventario;
}
