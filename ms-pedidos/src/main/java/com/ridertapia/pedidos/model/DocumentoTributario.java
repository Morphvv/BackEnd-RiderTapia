package com.ridertapia.pedidos.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documentos_tributarios")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class DocumentoTributario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDocumentoTributario;

    @Column(nullable = false)
    private Integer folio;

    @Column(nullable = false)
    private LocalDate fechaEmision;

    @Column(nullable = false)
    private Integer montoNeto;

    @Column(nullable = false)
    private Integer iva;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = true, length = 255)
    private String pdfUrl;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;
}
