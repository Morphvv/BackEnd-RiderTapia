package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.ContactoDto;
import cl.ridertapia.soporteservice.model.Contacto;
import cl.ridertapia.soporteservice.service.ContactoService;
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
@RequestMapping("/api/v1/contactos")
@RequiredArgsConstructor
@Tag(name = "Contacto", description = "Mensajes de contacto de clientes y visitantes, y su respuesta")
public class ContactoController {

    private final ContactoService contactoService;
    private final ContactoModelAssembler assembler;

    @Operation(summary = "Enviar un mensaje de contacto",
            description = "El RUT es opcional: un visitante sin cuenta también puede escribir. Si se envía, debe tener cuenta.")
    @ApiResponse(responseCode = "201", description = "Mensaje recibido (estado ABIERTO)")
    @ApiResponse(responseCode = "400", description = "Datos inválidos")
    @ApiResponse(responseCode = "404", description = "El RUT enviado no tiene cuenta")
    @ApiResponse(responseCode = "503", description = "usuario-service no está disponible")
    @PostMapping
    public ResponseEntity<EntityModel<ContactoDto.Respuesta>> enviarContacto(@Valid @RequestBody ContactoDto.Crear request) {
        EntityModel<ContactoDto.Respuesta> modelo = assembler.toModel(contactoService.enviarContacto(request));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @Operation(summary = "Listar mensajes de contacto", description = "Todos, o solo los del estado indicado.")
    @ApiResponse(responseCode = "200", description = "Lista de mensajes")
    @GetMapping
    public CollectionModel<EntityModel<ContactoDto.Respuesta>> listarContactos(
            @Parameter(description = "ABIERTO o RESPONDIDO") @RequestParam(required = false) Contacto.Estado estado) {
        return assembler.toCollectionModel(contactoService.listarContactos(estado))
                .add(linkTo(methodOn(ContactoController.class).listarContactos(estado)).withSelfRel());
    }

    @Operation(summary = "Obtener un mensaje por id")
    @ApiResponse(responseCode = "200", description = "Mensaje encontrado")
    @ApiResponse(responseCode = "404", description = "No existe el mensaje")
    @GetMapping("/{id}")
    public EntityModel<ContactoDto.Respuesta> findById(@PathVariable Long id) {
        return assembler.toModel(contactoService.findById(id));
    }

    @Operation(summary = "Responder un mensaje", description = "Guarda la respuesta y pasa el mensaje a RESPONDIDO.")
    @ApiResponse(responseCode = "200", description = "Mensaje respondido")
    @ApiResponse(responseCode = "400", description = "La respuesta está vacía")
    @ApiResponse(responseCode = "404", description = "No existe el mensaje")
    @PutMapping("/{id}/responder")
    public EntityModel<ContactoDto.Respuesta> responderContacto(@PathVariable Long id,
                                                               @Valid @RequestBody ContactoDto.Responder request) {
        return assembler.toModel(contactoService.responderContacto(id, request));
    }
}
