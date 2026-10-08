package com.ridertapia.productos.model;


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
@Table(name = "variantes_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VarianteProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVarianteProducto;

    @Column(nullable = false, length = 30, unique = true)
    private String sku;

    @Column(nullable = true, length = 10)
    private String talla;

    @Column(nullable = true, length = 30)
    private String color;

    @Column(nullable = true, length = 10)
    private String aro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
}
