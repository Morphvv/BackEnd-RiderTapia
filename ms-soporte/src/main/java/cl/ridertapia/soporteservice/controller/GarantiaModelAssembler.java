package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.GarantiaDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class GarantiaModelAssembler implements RepresentationModelAssembler<GarantiaDto.Respuesta, EntityModel<GarantiaDto.Respuesta>> {

    @Override
    public EntityModel<GarantiaDto.Respuesta> toModel(GarantiaDto.Respuesta g) {
        return EntityModel.of(g,
                linkTo(methodOn(GarantiaController.class).findById(g.idGarantia())).withSelfRel(),
                linkTo(methodOn(GarantiaController.class).verificarVigencia(g.idGarantia())).withRel("vigencia"),
                linkTo(methodOn(GarantiaController.class).findByRutUsuario(g.rutUsuario())).withRel("usuario"),
                linkTo(methodOn(GarantiaController.class).listarGarantias()).withRel("garantias"));
    }
}
