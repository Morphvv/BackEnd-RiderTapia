package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.SeguimientoDto;
import cl.ridertapia.soporteservice.service.SeguimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seguimientos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Solo para desarrollo
@Tag(name = "Seguimiento", description = "Seguimiento posterior a la compra (llamada, correo o encuesta)")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;
    private final SeguimientoModelAssembler assembler;

    @Operation(summary = "Registrar un seguimiento", description = "Queda en estado PENDIENTE.")
    @ApiResponse(responseCode = "201", description = "Seguimiento registrado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o tipo inexistente")
    @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese RUT")
    @ApiResponse(responseCode = "503", description = "usuario-service no está disponible")
    @PostMapping
    public ResponseEntity<EntityModel<SeguimientoDto.Respuesta>> registrarSeguimiento(
            @Valid @RequestBody SeguimientoDto.Crear request) {
        EntityModel<SeguimientoDto.Respuesta> modelo = assembler.toModel(seguimientoService.registrarSeguimiento(request));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @Operation(summary = "Listar todos los seguimientos")
    @ApiResponse(responseCode = "200", description = "Lista de seguimientos")
    @GetMapping
    public CollectionModel<EntityModel<SeguimientoDto.Respuesta>> listarSeguimientos() {
        return assembler.toCollectionModel(seguimientoService.listarSeguimientos())
                .add(linkTo(methodOn(SeguimientoController.class).listarSeguimientos()).withSelfRel());
    }

    @Operation(summary = "Obtener un seguimiento por id")
    @ApiResponse(responseCode = "200", description = "Seguimiento encontrado")
    @ApiResponse(responseCode = "404", description = "No existe el seguimiento")
    @GetMapping("/{id}")
    public EntityModel<SeguimientoDto.Respuesta> findById(@PathVariable Long id) {
        return assembler.toModel(seguimientoService.findById(id));
    }

    @Operation(summary = "Seguimientos de un pedido")
    @ApiResponse(responseCode = "200", description = "Seguimientos del pedido (puede ser una lista vacía)")
    @GetMapping("/pedido/{idPedido}")
    public CollectionModel<EntityModel<SeguimientoDto.Respuesta>> findByPedido(@PathVariable Long idPedido) {
        return assembler.toCollectionModel(seguimientoService.findByPedido(idPedido))
                .add(linkTo(methodOn(SeguimientoController.class).findByPedido(idPedido)).withSelfRel());
    }

    @Operation(summary = "Actualizar el estado de un seguimiento", description = "PENDIENTE, REALIZADO o CANCELADO.")
    @ApiResponse(responseCode = "200", description = "Seguimiento actualizado")
    @ApiResponse(responseCode = "400", description = "Estado inexistente")
    @ApiResponse(responseCode = "404", description = "No existe el seguimiento")
    @PutMapping("/{id}/estado")
    public EntityModel<SeguimientoDto.Respuesta> actualizarEstado(@PathVariable Long id,
                                                                 @Valid @RequestBody SeguimientoDto.ActualizarEstado request) {
        return assembler.toModel(seguimientoService.actualizarEstado(id, request));
    }
}
