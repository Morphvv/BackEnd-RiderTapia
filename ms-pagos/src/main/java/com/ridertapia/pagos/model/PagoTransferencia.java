package com.ridertapia.pagos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "pagos_transferencia")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class PagoTransferencia extends Pago {

    @Column(nullable = true, length = 255)
    private String comprobanteUrl;

    @Column(nullable = false)
    private LocalDateTime fechaLimite;

    @Column(nullable = true, length = 255)
    private String observacion;

    public PagoTransferencia(Long idPago, Long idPedido, Integer monto, LocalDateTime fecha, EstadoPago estado, String comprobanteUrl, LocalDateTime fechaLimite, String observacion) {
        super(idPago, idPedido, monto, fecha, estado);
        this.comprobanteUrl = comprobanteUrl;
        this.fechaLimite = fechaLimite;
        this.observacion = observacion;
    }
}
