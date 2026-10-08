package cl.ridertapia.soporteservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "resenas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"rut_usuario", "id_producto"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Resena {

    public enum Estado { VISIBLE, OCULTA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResena;

    @Column(name = "rut_usuario", nullable = false, length = 12)
    private String rutUsuario;

    // Referencia por ID al servicio de productos
    @Column(name = "id_producto", nullable = false)
    private Long idProducto;

    @Column(nullable = false)
    private Integer calificacion;

    @Column(length = 500)
    private String comentario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;
}
