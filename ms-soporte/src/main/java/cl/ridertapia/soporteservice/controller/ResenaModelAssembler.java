package cl.ridertapia.soporteservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.soporteservice.dto.ResenaDto;
import cl.ridertapia.soporteservice.model.Resena;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class ResenaModelAssembler implements RepresentationModelAssembler<ResenaDto.Respuesta, EntityModel<ResenaDto.Respuesta>> {

    @Override
    public EntityModel<ResenaDto.Respuesta> toModel(ResenaDto.Respuesta r) {
        EntityModel<ResenaDto.Respuesta> modelo = EntityModel.of(r,
                linkTo(methodOn(ResenaController.class).findById(r.idResena())).withSelfRel(),
                linkTo(methodOn(ResenaController.class).findByProducto(r.idProducto())).withRel("producto"),
                linkTo(methodOn(ResenaController.class).resumenProducto(r.idProducto())).withRel("resumen"),
                linkTo(methodOn(ResenaController.class).findByRutUsuario(r.rutUsuario())).withRel("usuario"),
                linkTo(methodOn(ResenaController.class).listarResenas()).withRel("resenas"));
        // Solo una reseña visible se puede ocultar
        if (r.estado() == Resena.Estado.VISIBLE) {
            modelo.add(linkTo(methodOn(ResenaController.class).ocultarResena(r.idResena())).withRel("ocultar"));
        }
        return modelo;
    }
}
