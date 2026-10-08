package cl.ridertapia.reportesservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "reportes_ventas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "fecha_inicio", "fecha_fin"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ReporteVentas {

    public enum Tipo { DIARIO, SEMANAL, MENSUAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReporte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipo tipo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    // Suma de los totales de los pedidos del período, en pesos
    @Column(nullable = false)
    private Long totalVentas;

    @Column(nullable = false)
    private Integer cantidadPedidos;

    @Column(nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(length = 60)
    private String generadoPor;
}
