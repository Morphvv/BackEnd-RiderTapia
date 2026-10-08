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
@Table(name = "imagenes_producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImagenProducto;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(nullable = false)
    private Integer orden;

    @Column(nullable = false)
    private Boolean esPrincipal;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;
}
