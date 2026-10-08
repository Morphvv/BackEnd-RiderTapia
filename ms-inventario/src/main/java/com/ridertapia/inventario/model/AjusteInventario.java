package com.ridertapia.inventario.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "ajustes_inventario")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class AjusteInventario extends MovimientoInventario {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MotivoAjuste motivo;

    @ManyToOne
    @JoinColumn(name = "id_toma_inventario")
    private TomaInventario tomaInventario;

    public AjusteInventario(Long idMovimientoInventario, TipoMovimiento tipo, Integer cantidad, Integer stockAnterior, Integer stockResultante, LocalDateTime fecha, String observacion, Long idEmpleado, Long idPedido, Long idOrdenTrabajo, ItemInventario itemInventario, MotivoAjuste motivo, TomaInventario tomaInventario) {
        super(idMovimientoInventario, tipo, cantidad, stockAnterior, stockResultante, fecha, observacion, idEmpleado, idPedido, idOrdenTrabajo, itemInventario);
        this.motivo = motivo;
        this.tomaInventario = tomaInventario;
    }
}
