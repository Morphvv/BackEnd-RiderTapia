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
@Table(name = "contactos")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Contacto {

    public enum Estado { ABIERTO, RESPONDIDO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idContacto;

    // Opcional: vacío cuando escribe un visitante sin cuenta
    @Column(name = "rut_usuario", length = 12)
    private String rutUsuario;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String emailContacto;

    @Column(nullable = false, length = 150)
    private String asunto;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Estado estado;

    @Column(length = 1000)
    private String respuesta;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
