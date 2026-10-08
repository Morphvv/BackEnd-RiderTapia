package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.DevolucionDto;
import cl.ridertapia.soporteservice.model.Devolucion;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class DevolucionModelAssembler implements RepresentationModelAssembler<DevolucionDto.Respuesta, EntityModel<DevolucionDto.Respuesta>> {

    @Override
    public EntityModel<DevolucionDto.Respuesta> toModel(DevolucionDto.Respuesta d) {
        EntityModel<DevolucionDto.Respuesta> modelo = EntityModel.of(d,
                linkTo(methodOn(DevolucionController.class).findById(d.idDevolucion())).withSelfRel(),
                linkTo(methodOn(DevolucionController.class).findByRutUsuario(d.rutUsuario())).withRel("usuario"),
                linkTo(methodOn(DevolucionController.class).listarDevoluciones(null)).withRel("devoluciones"));
        // Solo se puede resolver una devolución que aún está en curso (SOLICITADA o APROBADA)
        if (d.estado() == Devolucion.Estado.SOLICITADA || d.estado() == Devolucion.Estado.APROBADA) {
            modelo.add(linkTo(methodOn(DevolucionController.class).resolverDevolucion(d.idDevolucion(), null))
                    .withRel("resolver"));
        }
        return modelo;
    }
}
