package com.ridertapia.pedidos.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "boletas")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class Boleta extends DocumentoTributario {

    public Boleta(Long idDocumentoTributario, Integer folio, LocalDate fechaEmision, Integer montoNeto, Integer iva, Integer total, String pdfUrl, Pedido pedido) {
        super(idDocumentoTributario, folio, fechaEmision, montoNeto, iva, total, pdfUrl, pedido);
    }
}
