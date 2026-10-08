package com.ridertapia.productos.model;

import java.time.LocalDate;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cupones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCupon;

    @Column(nullable = false, length = 30, unique = true)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDescuento tipoDescuento;

    @Column(nullable = false)
    private Integer valor;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaTermino;

    @Column(nullable = false)
    private Integer limiteUsos;

    @Column(nullable = false)
    private Integer usosActuales;

    @Column(nullable = false)
    private Boolean activo;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "cupones_categorias",
            joinColumns = @JoinColumn(name = "id_cupon"),
            inverseJoinColumns = @JoinColumn(name = "id_categoria"))
    private List<Categoria> categorias = new ArrayList<>();
}
