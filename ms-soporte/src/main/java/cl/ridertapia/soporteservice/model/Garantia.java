package cl.ridertapia.soporteservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "garantias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Garantia {

    public enum Estado { VIGENTE, EXPIRADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGarantia;

    @Column(name = "rut_usuario", nullable = false, length = 12)
    private String rutUsuario;

    // Referencias por ID a los servicios de pedidos y productos
    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaTermino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;
}
