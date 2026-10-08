package com.ridertapia.pagos.model;

import java.time.LocalDate;

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
@Table(name = "cierres_caja")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCierreCaja;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private Integer totalEfectivo;

    @Column(nullable = false)
    private Integer totalDebito;

    @Column(nullable = false)
    private Integer totalCredito;

    @Column(nullable = false)
    private Integer totalTransferencia;

    @Column(nullable = false)
    private Integer efectivoContado;

    @Column(nullable = false)
    private Integer diferencia;

    @Column(nullable = false)
    private Long idEmpleado;
}
