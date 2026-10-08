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
@Table(name = "datos_factura")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DatosFactura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDatosFactura;

    @Column(name = "id_documento", nullable = false, unique = true)
    private Long idDocumento;

    @Column(nullable = false, length = 12)
    private String rutEmpresa;

    @Column(nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, length = 150)
    private String giro;

    @Column(nullable = false, length = 200)
    private String direccionEmpresa;
}
