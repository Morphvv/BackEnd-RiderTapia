package com.ridertapia.pedidos.model;

import java.time.LocalDateTime;

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
@Table(name = "notificaciones_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacionPedido;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 100)
    private String destinatario;

    @Column(nullable = false)
    private LocalDateTime fechaEnvio;

    @Column(nullable = false)
    private Boolean enviada;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;
}
