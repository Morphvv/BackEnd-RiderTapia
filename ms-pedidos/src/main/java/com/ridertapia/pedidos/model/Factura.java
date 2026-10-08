package com.ridertapia.pedidos.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "facturas")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class Factura extends DocumentoTributario {

    @Column(nullable = false, length = 12)
    private String rutEmpresa;

    @Column(nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, length = 150)
    private String giro;

    @Column(nullable = false, length = 255)
    private String direccionEmpresa;

    public Factura(Long idDocumentoTributario, Integer folio, LocalDate fechaEmision, Integer montoNeto, Integer iva, Integer total, String pdfUrl, Pedido pedido, String rutEmpresa, String razonSocial, String giro, String direccionEmpresa) {
        super(idDocumentoTributario, folio, fechaEmision, montoNeto, iva, total, pdfUrl, pedido);
        this.rutEmpresa = rutEmpresa;
        this.razonSocial = razonSocial;
        this.giro = giro;
        this.direccionEmpresa = direccionEmpresa;
    }
}
