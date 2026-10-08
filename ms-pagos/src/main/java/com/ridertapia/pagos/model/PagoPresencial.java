package com.ridertapia.pagos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "pagos_presenciales")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class PagoPresencial extends Pago {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedioPago medioPago;

    public PagoPresencial(Long idPago, Long idPedido, Integer monto, LocalDateTime fecha, EstadoPago estado, MedioPago medioPago) {
        super(idPago, idPedido, monto, fecha, estado);
        this.medioPago = medioPago;
    }
}
