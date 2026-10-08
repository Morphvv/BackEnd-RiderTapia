package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.SeguimientoDto;
import cl.ridertapia.soporteservice.model.Seguimiento;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class SeguimientoModelAssembler implements RepresentationModelAssembler<SeguimientoDto.Respuesta, EntityModel<SeguimientoDto.Respuesta>> {

    @Override
    public EntityModel<SeguimientoDto.Respuesta> toModel(SeguimientoDto.Respuesta s) {
        EntityModel<SeguimientoDto.Respuesta> modelo = EntityModel.of(s,
                linkTo(methodOn(SeguimientoController.class).findById(s.idSeguimiento())).withSelfRel(),
                linkTo(methodOn(SeguimientoController.class).findByPedido(s.idPedido())).withRel("pedido"),
                linkTo(methodOn(SeguimientoController.class).listarSeguimientos()).withRel("seguimientos"));
        // Solo un seguimiento pendiente se puede actualizar
        if (s.estado() == Seguimiento.Estado.PENDIENTE) {
            modelo.add(linkTo(methodOn(SeguimientoController.class).actualizarEstado(s.idSeguimiento(), null))
                    .withRel("actualizarEstado"));
        }
        return modelo;
    }
}
