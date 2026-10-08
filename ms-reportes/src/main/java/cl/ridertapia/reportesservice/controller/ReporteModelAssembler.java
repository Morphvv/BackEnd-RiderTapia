package cl.ridertapia.reportesservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.reportesservice.dto.ReporteDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

@Component
public class ReporteModelAssembler {

    public EntityModel<ReporteDto.Detalle> detalle(ReporteDto.Detalle detalle) {
        Long id = detalle.reporte().idReporte();
        return EntityModel.of(detalle,
                linkTo(methodOn(ReporteController.class).findById(id)).withSelfRel(),
                linkTo(methodOn(ReporteController.class).listarMasVendidos(id)).withRel("masVendidos"),
                linkTo(methodOn(ReporteController.class).listarReportes(null)).withRel("reportes"));
    }

    public EntityModel<ReporteDto.Resumen> resumen(ReporteDto.Resumen resumen) {
        return EntityModel.of(resumen,
                linkTo(methodOn(ReporteController.class).findById(resumen.idReporte())).withSelfRel(),
                linkTo(methodOn(ReporteController.class).listarMasVendidos(resumen.idReporte())).withRel("masVendidos"));
    }
}
