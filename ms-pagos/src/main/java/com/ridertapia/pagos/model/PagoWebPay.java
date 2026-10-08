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
@Table(name = "pagos_webpay")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class PagoWebPay extends Pago {

    @Column(nullable = false, length = 100)
    private String token;

    @Column(nullable = true, length = 50)
    private String codigoAutorizacion;

    @Column(nullable = true, length = 4)
    private String ultimosDigitosTarjeta;

    public PagoWebPay(Long idPago, Long idPedido, Integer monto, LocalDateTime fecha, EstadoPago estado, String token, String codigoAutorizacion, String ultimosDigitosTarjeta) {
        super(idPago, idPedido, monto, fecha, estado);
        this.token = token;
        this.codigoAutorizacion = codigoAutorizacion;
        this.ultimosDigitosTarjeta = ultimosDigitosTarjeta;
    }
}
