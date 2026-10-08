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
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    @Column(nullable = false, length = 50, unique = true)
    private String nombre;

    @Column(nullable = true, length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Boolean activa;

    @ManyToOne
    @JoinColumn(name = "id_categoria_padre")
    private Categoria categoriaPadre;
}
