package cl.ridertapia.clienteservice.dto;

import cl.ridertapia.clienteservice.model.Cliente;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

@Relation(collectionRelation = "clientes", itemRelation = "cliente")
@Schema(description = "Datos de un cliente")
public record ClienteResponse(
        @Schema(example = "12345678-5") String rutUsuario,
        @Schema(example = "Matias") String nombre,
        @Schema(example = "Dominguez") String apellido,
        @Schema(example = "+56912345678") String telefono,
        @Schema(example = "Calle Falsa 123") String direccion,
        @Schema(example = "Concepción") String comuna
) {

    public static ClienteResponse desde(Cliente cliente) {
        return new ClienteResponse(
                cliente.getRutUsuario(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getComuna());
    }
}
