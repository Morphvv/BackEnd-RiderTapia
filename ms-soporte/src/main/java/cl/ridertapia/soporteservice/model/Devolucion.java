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
@Table(name = "devoluciones")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Devolucion {

    public enum Estado { SOLICITADA, APROBADA, RECHAZADA, COMPLETADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDevolucion;

    @Column(name = "rut_usuario", nullable = false, length = 12)
    private String rutUsuario;

    // Referencias por ID a los servicios de pedidos y productos
    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaResolucion;

    // Respuesta del administrador al aprobar o rechazar
    @Column(length = 500)
    private String respuesta;
}
