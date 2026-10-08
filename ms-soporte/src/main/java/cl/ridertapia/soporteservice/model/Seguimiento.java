package cl.ridertapia.soporteservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Table(name = "seguimientos")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Seguimiento {

    public enum Tipo { LLAMADA, CORREO, ENCUESTA }

    public enum Estado { PENDIENTE, REALIZADO, CANCELADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSeguimiento;

    // Referencia por ID al servicio de pedidos
    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Column(name = "rut_usuario", nullable = false, length = 12)
    private String rutUsuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    @Column(length = 500)
    private String observacion;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
