package cl.ridertapia.reportesservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "productos_mas_vendidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductoMasVendido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRegistro;

    @Column(name = "id_reporte", nullable = false)
    private Long idReporte;

    @Column(name = "id_variante", nullable = false)
    private Long idVariante;

    // Queda vacío mientras no se consulte el servicio de productos
    @Column(length = 150)
    private String nombreProducto;

    @Column(nullable = false)
    private Integer cantidadVendida;

    // Suma de los subtotales de las líneas, antes de descuentos del pedido
    @Column(nullable = false)
    private Long totalVendido;
}
