package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.ResenaDto;
import cl.ridertapia.soporteservice.service.ResenaService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/soporte/resenas")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Opiniones y calificaciones (1 a 5) de los clientes sobre los productos")
public class ResenaController {

    private final ResenaService resenaService;
    private final ResenaModelAssembler assembler;

    @Operation(summary = "Publicar una reseña", description = "Cada usuario puede reseñar un producto una sola vez.")
    @ApiResponse(responseCode = "201", description = "Reseña publicada")
    @ApiResponse(responseCode = "400", description = "Datos inválidos (calificación fuera de 1 a 5, RUT mal escrito...)")
    @ApiResponse(responseCode = "404", description = "No existe una cuenta con ese RUT")
    @ApiResponse(responseCode = "409", description = "El usuario ya reseñó ese producto")
    @ApiResponse(responseCode = "503", description = "usuario-service no está disponible")
    @PostMapping
    public ResponseEntity<EntityModel<ResenaDto.Respuesta>> guardarResena(@Valid @RequestBody ResenaDto.Crear request) {
        EntityModel<ResenaDto.Respuesta> modelo = assembler.toModel(resenaService.guardarResena(request));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @Operation(summary = "Listar todas las reseñas", description = "Incluye las ocultas (uso del administrador).")
    @ApiResponse(responseCode = "200", description = "Lista de reseñas")
    @GetMapping
    public CollectionModel<EntityModel<ResenaDto.Respuesta>> listarResenas() {
        return assembler.toCollectionModel(resenaService.listarResenas())
                .add(linkTo(methodOn(ResenaController.class).listarResenas()).withSelfRel());
    }

    @Operation(summary = "Obtener una reseña por id")
    @ApiResponse(responseCode = "200", description = "Reseña encontrada")
    @ApiResponse(responseCode = "404", description = "No existe la reseña")
    @GetMapping("/{id}")
    public EntityModel<ResenaDto.Respuesta> findById(@PathVariable Long id) {
        return assembler.toModel(resenaService.findById(id));
    }

    @Operation(summary = "Reseñas visibles de un producto", description = "Lo que ve el público: las ocultas no aparecen.")
    @ApiResponse(responseCode = "200", description = "Reseñas del producto (puede ser una lista vacía)")
    @GetMapping("/producto/{idProducto}")
    public CollectionModel<EntityModel<ResenaDto.Respuesta>> findByProducto(@PathVariable Long idProducto) {
        return assembler.toCollectionModel(resenaService.findByProducto(idProducto))
                .add(linkTo(methodOn(ResenaController.class).findByProducto(idProducto)).withSelfRel())
                .add(linkTo(methodOn(ResenaController.class).resumenProducto(idProducto)).withRel("resumen"));
    }

    @Operation(summary = "Promedio de calificaciones de un producto",
            description = "Considera solo las reseñas visibles. Sin reseñas devuelve promedio 0.")
    @ApiResponse(responseCode = "200", description = "Promedio y cantidad de reseñas")
    @GetMapping("/producto/{idProducto}/resumen")
    public EntityModel<ResenaDto.Resumen> resumenProducto(@PathVariable Long idProducto) {
        return EntityModel.of(resenaService.resumenProducto(idProducto),
                linkTo(methodOn(ResenaController.class).resumenProducto(idProducto)).withSelfRel(),
                linkTo(methodOn(ResenaController.class).findByProducto(idProducto)).withRel("resenas"));
    }

    @Operation(summary = "Reseñas escritas por un usuario")
    @ApiResponse(responseCode = "200", description = "Reseñas del usuario (puede ser una lista vacía)")
    @GetMapping("/usuario/{rut}")
    public CollectionModel<EntityModel<ResenaDto.Respuesta>> findByRutUsuario(
            @Parameter(description = "RUT con guion", example = "12345678-5") @PathVariable String rut) {
        return assembler.toCollectionModel(resenaService.findByRutUsuario(rut))
                .add(linkTo(methodOn(ResenaController.class).findByRutUsuario(rut)).withSelfRel());
    }

    @Operation(summary = "Ocultar una reseña", description = "La reseña deja de mostrarse al público y de contar en el promedio.")
    @ApiResponse(responseCode = "200", description = "Reseña oculta")
    @ApiResponse(responseCode = "404", description = "No existe la reseña")
    @PatchMapping("/{id}/ocultar")
    public EntityModel<ResenaDto.Respuesta> ocultarResena(@PathVariable Long id) {
        return assembler.toModel(resenaService.ocultarResena(id));
    }
}
