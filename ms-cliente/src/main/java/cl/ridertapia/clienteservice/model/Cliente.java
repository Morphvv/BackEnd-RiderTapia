package cl.ridertapia.clienteservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data 
@Entity
@Table(name = "clientes")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Cliente {

    @Id
    @Column(name = "rut_usuario", length = 12)
    private String rutUsuario;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @Column(length = 15)
    private String telefono;

    @Column(length = 150)
    private String direccion;

    @Column(length = 60)
    private String comuna;
}
