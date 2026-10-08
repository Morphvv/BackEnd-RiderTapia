package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.DevolucionDto;
import cl.ridertapia.soporteservice.model.Devolucion;
import cl.ridertapia.soporteservice.service.DevolucionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/devoluciones")
@RequiredArgsConstructor
@Tag(name = "Devoluciones", description = "Solicitudes de devolución y su resolución")
public class DevolucionController {

    private final DevolucionService devolucionService;
    private final DevolucionModelAssembler assembler;

    @Operation(summary = "Solicitar una devolución",
            description = "Queda en estado SOLICITADA. No se acepta otra mientras haya una en curso para el mismo producto del pedido.")
    @ApiResponse(responseCode = "201", description = "Devolución solicitada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (el motivo debe tener al menos 10 caracteres)")
    @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese RUT")
    @ApiResponse(responseCode = "409", description = "Ya hay una devolución en curso para ese producto")
    @ApiResponse(responseCode = "503", description = "usuario-service no está disponible")
    @PostMapping
    public ResponseEntity<EntityModel<DevolucionDto.Respuesta>> solicitarDevolucion(
            @Valid @RequestBody DevolucionDto.Crear request) {
        EntityModel<DevolucionDto.Respuesta> modelo = assembler.toModel(devolucionService.solicitarDevolucion(request));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @Operation(summary = "Listar devoluciones", description = "Todas, o solo las del estado indicado.")
    @ApiResponse(responseCode = "200", description = "Lista de devoluciones")
    @GetMapping
    public CollectionModel<EntityModel<DevolucionDto.Respuesta>> listarDevoluciones(
            @Parameter(description = "SOLICITADA, APROBADA, RECHAZADA o COMPLETADA")
            @RequestParam(required = false) Devolucion.Estado estado) {
        return assembler.toCollectionModel(devolucionService.listarDevoluciones(estado))
                .add(linkTo(methodOn(DevolucionController.class).listarDevoluciones(estado)).withSelfRel());
    }

    @Operation(summary = "Obtener una devolución por id")
    @ApiResponse(responseCode = "200", description = "Devolución encontrada")
    @ApiResponse(responseCode = "404", description = "No existe la devolución")
    @GetMapping("/{id}")
    public EntityModel<DevolucionDto.Respuesta> findById(@PathVariable Long id) {
        return assembler.toModel(devolucionService.findById(id));
    }

    @Operation(summary = "Devoluciones de un usuario")
    @ApiResponse(responseCode = "200", description = "Devoluciones del usuario (puede ser una lista vacía)")
    @GetMapping("/usuario/{rut}")
    public CollectionModel<EntityModel<DevolucionDto.Respuesta>> findByRutUsuario(
            @Parameter(description = "RUT con guion", example = "12345678-5") @PathVariable String rut) {
        return assembler.toCollectionModel(devolucionService.findByRutUsuario(rut))
                .add(linkTo(methodOn(DevolucionController.class).findByRutUsuario(rut)).withSelfRel());
    }

    @Operation(summary = "Resolver una devolución",
            description = "Flujo permitido: SOLICITADA pasa a APROBADA o RECHAZADA, y APROBADA pasa a COMPLETADA. "
                    + "Al rechazar es obligatorio explicar el motivo en 'respuesta'.")
    @ApiResponse(responseCode = "200", description = "Devolución actualizada")
    @ApiResponse(responseCode = "400", description = "Falta el motivo del rechazo o el estado no existe")
    @ApiResponse(responseCode = "404", description = "No existe la devolución")
    @ApiResponse(responseCode = "409", description = "Cambio de estado no permitido")
    @PutMapping("/{id}/resolver")
    public EntityModel<DevolucionDto.Respuesta> resolverDevolucion(@PathVariable Long id,
                                                                  @Valid @RequestBody DevolucionDto.Resolver request) {
        return assembler.toModel(devolucionService.resolverDevolucion(id, request));
    }
}
