package cl.ridertapia.clienteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.clienteservice.dto.ClienteRequest;
import cl.ridertapia.clienteservice.dto.ClienteResponse;
import cl.ridertapia.clienteservice.dto.ClienteUpdateRequest;
import cl.ridertapia.clienteservice.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de la información personal y de despacho de los clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteModelAssembler assembler;

    @Operation(summary = "Crear el perfil de un cliente",
            description = "El RUT debe tener una cuenta con rol CLIENTE en usuario-service.")
    @ApiResponse(responseCode = "201", description = "Perfil creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o la cuenta no tiene rol CLIENTE")
    @ApiResponse(responseCode = "404", description = "No existe una cuenta de usuario con ese RUT")
    @ApiResponse(responseCode = "409", description = "Ya existe un perfil con ese RUT")
    @ApiResponse(responseCode = "503", description = "usuario-service no está disponible")
    @PostMapping
    public ResponseEntity<EntityModel<ClienteResponse>> guardarCliente(@Valid @RequestBody ClienteRequest request) {
        EntityModel<ClienteResponse> modelo = assembler.toModel(clienteService.guardarCliente(request));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @Operation(summary = "Listar todos los clientes")
    @ApiResponse(responseCode = "200", description = "Lista de clientes")
    @GetMapping
    public CollectionModel<EntityModel<ClienteResponse>> listarClientes() {
        List<ClienteResponse> clientes = clienteService.listarClientes();
        return assembler.toCollectionModel(clientes)
                .add(linkTo(methodOn(ClienteController.class).listarClientes()).withSelfRel());
    }

    @Operation(summary = "Buscar un cliente por RUT")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un cliente con ese RUT")
    @GetMapping("/{rut}")
    public EntityModel<ClienteResponse> findByRut(
            @Parameter(description = "RUT con guion, ej: 12345678-5", example = "12345678-5")
            @PathVariable String rut) {
        return assembler.toModel(clienteService.findByRut(rut));
    }

    @Operation(summary = "Buscar clientes por nombre o apellido",
            description = "Coincidencia parcial, sin distinguir mayúsculas.")
    @ApiResponse(responseCode = "200", description = "Clientes que coinciden (puede ser una lista vacía)")
    @GetMapping("/buscar")
    public CollectionModel<EntityModel<ClienteResponse>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en nombre o apellido", example = "Matias")
            @RequestParam String nombre) {
        return assembler.toCollectionModel(clienteService.buscarPorNombre(nombre))
                .add(linkTo(methodOn(ClienteController.class).listarClientes()).withRel("clientes"));
    }

    @Operation(summary = "Actualizar los datos de un cliente",
            description = "Reemplaza nombre, apellido, teléfono, dirección y comuna. El RUT no se puede cambiar.")
    @ApiResponse(responseCode = "200", description = "Cliente actualizado")
    @ApiResponse(responseCode = "404", description = "No existe un cliente con ese RUT")
    @PutMapping("/{rut}")
    public EntityModel<ClienteResponse> actualizarDatos(@PathVariable String rut,
                                                        @Valid @RequestBody ClienteUpdateRequest request) {
        return assembler.toModel(clienteService.actualizarDatos(rut, request));
    }
}
