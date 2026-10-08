package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.ContactoDto;
import cl.ridertapia.soporteservice.model.Contacto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ContactoModelAssembler implements RepresentationModelAssembler<ContactoDto.Respuesta, EntityModel<ContactoDto.Respuesta>> {

    @Override
    public EntityModel<ContactoDto.Respuesta> toModel(ContactoDto.Respuesta c) {
        EntityModel<ContactoDto.Respuesta> modelo = EntityModel.of(c,
                linkTo(methodOn(ContactoController.class).findById(c.idContacto())).withSelfRel(),
                linkTo(methodOn(ContactoController.class).listarContactos(null)).withRel("contactos"));
        // Solo un mensaje abierto se puede responder
        if (c.estado() == Contacto.Estado.ABIERTO) {
            modelo.add(linkTo(methodOn(ContactoController.class).responderContacto(c.idContacto(), null))
                    .withRel("responder"));
        }
        return modelo;
    }
}
