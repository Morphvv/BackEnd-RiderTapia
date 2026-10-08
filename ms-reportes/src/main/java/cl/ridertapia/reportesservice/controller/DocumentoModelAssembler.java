package cl.ridertapia.reportesservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.reportesservice.dto.DocumentoDto;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

@Component
public class DocumentoModelAssembler {

    public EntityModel<DocumentoDto.Detalle> detalle(DocumentoDto.Detalle detalle) {
        DocumentoDto.Resumen doc = detalle.documento();
        return EntityModel.of(detalle,
                linkTo(methodOn(DocumentoVentaController.class).findById(doc.idDocumento())).withSelfRel(),
                linkTo(methodOn(DocumentoVentaController.class).findByPedido(doc.idPedido())).withRel("pedido"),
                linkTo(methodOn(DocumentoVentaController.class).listarDocumentos(null)).withRel("documentos"));
    }

    public EntityModel<DocumentoDto.Resumen> resumen(DocumentoDto.Resumen doc) {
        return EntityModel.of(doc,
                linkTo(methodOn(DocumentoVentaController.class).findById(doc.idDocumento())).withSelfRel(),
                linkTo(methodOn(DocumentoVentaController.class).findByPedido(doc.idPedido())).withRel("pedido"));
    }
}
