package cl.ridertapia.usuarioservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.usuarioservice.dto.LoginRequest;
import cl.ridertapia.usuarioservice.dto.RecuperarPasswordRequest;
import cl.ridertapia.usuarioservice.dto.UsuarioRequest;
import cl.ridertapia.usuarioservice.dto.UsuarioResponse;
import cl.ridertapia.usuarioservice.dto.UsuarioUpdateRequest;
import cl.ridertapia.usuarioservice.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de cuentas de acceso (clientes y personal interno)")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioModelAssembler assembler;

    @Operation(summary = "Registrar un usuario nuevo",
            description = "Si no se indica rol, se crea como CLIENTE. La contraseña se guarda cifrada.")
    @ApiResponse(responseCode = "201", description = "Usuario creado")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o RUT con dígito verificador incorrecto")
    @ApiResponse(responseCode = "409", description = "Ya existe un usuario con ese RUT o email")
    @PostMapping
    public ResponseEntity<EntityModel<UsuarioResponse>> guardarUsuario(@Valid @RequestBody UsuarioRequest request) {
        EntityModel<UsuarioResponse> modelo = assembler.toModel(usuarioService.guardarUsuario(request));
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }

    @Operation(summary = "Listar todos los usuarios")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios (sin contraseñas)")
    @GetMapping
    public CollectionModel<EntityModel<UsuarioResponse>> listarUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.listarUsuarios();
        return assembler.toCollectionModel(usuarios)
                .add(linkTo(methodOn(UsuarioController.class).listarUsuarios()).withSelfRel());
    }

    @Operation(summary = "Buscar un usuario por RUT")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese RUT")
    @GetMapping("/{rut}")
    public EntityModel<UsuarioResponse> findByRut(
            @Parameter(description = "RUT con guion, ej: 12345678-5", example = "12345678-5")
            @PathVariable String rut) {
        return assembler.toModel(usuarioService.findByRut(rut));
    }

    @Operation(summary = "Buscar un usuario por email")
    @ApiResponse(responseCode = "200", description = "Usuario encontrado")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese email")
    @GetMapping("/buscar")
    public EntityModel<UsuarioResponse> findByEmailCliente(
            @Parameter(description = "Email del usuario", example = "pedro@correo.cl")
            @RequestParam String email) {
        return assembler.toModel(usuarioService.findByEmailCliente(email));
    }

    @Operation(summary = "Actualizar un usuario",
            description = "La contraseña y el rol son opcionales: si no se envían, se mantienen.")
    @ApiResponse(responseCode = "200", description = "Usuario actualizado")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese RUT")
    @ApiResponse(responseCode = "409", description = "El email ya pertenece a otro usuario")
    @PutMapping("/{rut}")
    public EntityModel<UsuarioResponse> actualizarUsuario(@PathVariable String rut,
                                                          @Valid @RequestBody UsuarioUpdateRequest request) {
        return assembler.toModel(usuarioService.actualizarUsuario(rut, request));
    }

    @Operation(summary = "Iniciar sesión")
    @ApiResponse(responseCode = "200", description = "Credenciales correctas")
    @ApiResponse(responseCode = "401", description = "Email o contraseña incorrectos")
    @PostMapping("/login")
    public EntityModel<UsuarioResponse> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return assembler.toModel(usuarioService.iniciarSesion(request));
    }

    @Operation(summary = "Recuperar contraseña",
            description = "Versión académica simplificada: cambia la clave conociendo solo el email.")
    @ApiResponse(responseCode = "200", description = "Contraseña actualizada")
    @ApiResponse(responseCode = "404", description = "No existe un usuario con ese email")
    @PostMapping("/recuperar-password")
    public Map<String, String> recuperarPassword(@Valid @RequestBody RecuperarPasswordRequest request) {
        usuarioService.recuperarPassword(request);
        return Map.of("mensaje", "Contraseña actualizada correctamente");
    }
}
