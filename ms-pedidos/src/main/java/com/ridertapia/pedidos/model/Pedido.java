package com.ridertapia.pedidos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPedido;

    @Column(nullable = false, length = 20, unique = true)
    private String numero;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = true)
    private LocalDateTime fechaExpiracion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CanalVenta canal;

    @Column(nullable = false)
    private Integer subtotal;

    @Column(nullable = false)
    private Integer descuento;

    @Column(nullable = false)
    private Integer total;

    @Column(nullable = true, length = 255)
    private String motivoAnulacion;

    @Column(nullable = true, length = 12)
    private String rutCliente;

    @Column(nullable = true)
    private Long idCupon;

    @Column(nullable = true)
    private Long idEmpleado;

    @ManyToOne
    @JoinColumn(name = "id_datos_visitante")
    private DatosVisitante datosVisitante;
}
