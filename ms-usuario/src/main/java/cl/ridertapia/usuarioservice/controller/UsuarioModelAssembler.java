package cl.ridertapia.usuarioservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.usuarioservice.dto.UsuarioResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UsuarioModelAssembler implements RepresentationModelAssembler<UsuarioResponse, EntityModel<UsuarioResponse>> {

    @Override
    public EntityModel<UsuarioResponse> toModel(UsuarioResponse usuario) {
        return EntityModel.of(usuario,
                linkTo(methodOn(UsuarioController.class).findByRut(usuario.rutUsuario())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listarUsuarios()).withRel("usuarios"));
    }
}
