package cl.ridertapia.clienteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.clienteservice.dto.ClienteResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ClienteModelAssembler implements RepresentationModelAssembler<ClienteResponse, EntityModel<ClienteResponse>> {

    @Override
    public EntityModel<ClienteResponse> toModel(ClienteResponse cliente) {
        return EntityModel.of(cliente,
                linkTo(methodOn(ClienteController.class).findByRut(cliente.rutUsuario())).withSelfRel(),
                linkTo(methodOn(ClienteController.class).listarClientes()).withRel("clientes"));
    }
}
