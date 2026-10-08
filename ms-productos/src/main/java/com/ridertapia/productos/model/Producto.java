package com.ridertapia.productos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = true, length = 500)
    private String descripcion;

    @Column(nullable = false)
    private Integer precioNormal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPublicacion estado;

    @Column(nullable = false)
    private Boolean destacado;

    @Column(nullable = true)
    private Integer ordenDestacado;

    @Column(nullable = true)
    private Integer limitePorCompra;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_marca", nullable = false)
    private Marca marca;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "productos_compatibilidades",
            joinColumns = @JoinColumn(name = "id_producto"),
            inverseJoinColumns = @JoinColumn(name = "id_compatibilidad"))
    private List<Compatibilidad> compatibilidades = new ArrayList<>();
}
