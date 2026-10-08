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
@Table(name = "detalles_conteo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleConteo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetalleConteo;

    @Column(nullable = false)
    private Integer cantidadSistema;

    @Column(nullable = false)
    private Integer cantidadContada;

    @Column(nullable = false)
    private Integer diferencia;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_toma_inventario", nullable = false)
    private TomaInventario tomaInventario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_item_inventario", nullable = false)
    private ItemInventario itemInventario;
}
