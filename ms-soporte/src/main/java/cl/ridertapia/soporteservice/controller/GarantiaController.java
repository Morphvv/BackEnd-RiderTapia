package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.GarantiaDto;
import cl.ridertapia.soporteservice.service.GarantiaService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/garantias")
@RequiredArgsConstructor
@Tag(name = "Garantías", description = "Garantías de los productos comprados")
public class GarantiaController {

    private final GarantiaService garantiaService;
    private final GarantiaModelAssembler assembler;

    @Operation(summary = "Registrar una garantía",
            description = "Empieza hoy y dura 12 meses salvo que se indique 'mesesCobertura' (1 a 60). Una por producto de cada pedido.")
    @ApiResponse(responseCode = "201", description = "Garantía registrada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese RUT")
    @ApiResponse(responseCode = "409", description = "Ya existe una garantía para ese producto de ese pedido")
    @ApiResponse(responseCode = "503", description = "usuario-service no está disponible")
    @PostMapping
    public ResponseEntity<EntityModel<GarantiaDto.Respuesta>> registrarGarantia(
            @Valid @RequestBody GarantiaDto.Crear request) {
        EntityModel<GarantiaDto.Respuesta> modelo = assembler.toModel(garantiaService.registrarGarantia(request));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @Operation(summary = "Listar todas las garantías")
    @ApiResponse(responseCode = "200", description = "Lista de garantías")
    @GetMapping
    public CollectionModel<EntityModel<GarantiaDto.Respuesta>> listarGarantias() {
        return assembler.toCollectionModel(garantiaService.listarGarantias())
                .add(linkTo(methodOn(GarantiaController.class).listarGarantias()).withSelfRel());
    }

    @Operation(summary = "Obtener una garantía por id")
    @ApiResponse(responseCode = "200", description = "Garantía encontrada")
    @ApiResponse(responseCode = "404", description = "No existe la garantía")
    @GetMapping("/{id}")
    public EntityModel<GarantiaDto.Respuesta> findById(@PathVariable Long id) {
        return assembler.toModel(garantiaService.findById(id));
    }

    @Operation(summary = "Garantías de un usuario")
    @ApiResponse(responseCode = "200", description = "Garantías del usuario (puede ser una lista vacía)")
    @GetMapping("/usuario/{rut}")
    public CollectionModel<EntityModel<GarantiaDto.Respuesta>> findByRutUsuario(
            @Parameter(description = "RUT con guion", example = "12345678-5") @PathVariable String rut) {
        return assembler.toCollectionModel(garantiaService.findByRutUsuario(rut))
                .add(linkTo(methodOn(GarantiaController.class).findByRutUsuario(rut)).withSelfRel());
    }

    @Operation(summary = "Verificar si una garantía sigue vigente",
            description = "Devuelve los días restantes. Si ya venció, la marca como EXPIRADA.")
    @ApiResponse(responseCode = "200", description = "Estado de la vigencia")
    @ApiResponse(responseCode = "404", description = "No existe la garantía")
    @GetMapping("/{id}/vigencia")
    public EntityModel<GarantiaDto.Vigencia> verificarVigencia(@PathVariable Long id) {
        return EntityModel.of(garantiaService.verificarVigencia(id),
                linkTo(methodOn(GarantiaController.class).verificarVigencia(id)).withSelfRel(),
                linkTo(methodOn(GarantiaController.class).findById(id)).withRel("garantia"));
    }
}
