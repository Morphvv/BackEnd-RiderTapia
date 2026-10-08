package com.ridertapia.inventario.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "ingresos_mercancia")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class IngresoMercancia extends MovimientoInventario {

    @Column(nullable = false)
    private Integer costoUnitario;

    @Column(nullable = false, length = 30)
    private String numeroDocumento;

    @Column(nullable = false)
    private Long idProveedor;

    public IngresoMercancia(Long idMovimientoInventario, TipoMovimiento tipo, Integer cantidad, Integer stockAnterior, Integer stockResultante, LocalDateTime fecha, String observacion, Long idEmpleado, Long idPedido, Long idOrdenTrabajo, ItemInventario itemInventario, Integer costoUnitario, String numeroDocumento, Long idProveedor) {
        super(idMovimientoInventario, tipo, cantidad, stockAnterior, stockResultante, fecha, observacion, idEmpleado, idPedido, idOrdenTrabajo, itemInventario);
        this.costoUnitario = costoUnitario;
        this.numeroDocumento = numeroDocumento;
        this.idProveedor = idProveedor;
    }
}
