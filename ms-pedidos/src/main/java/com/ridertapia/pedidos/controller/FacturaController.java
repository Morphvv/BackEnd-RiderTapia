package com.ridertapia.pedidos.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridertapia.pedidos.model.Factura;
import com.ridertapia.pedidos.service.FacturaService;

@RestController
@RequestMapping("/api/v1/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Factura>>> listarFacturas() {
        CollectionModel<EntityModel<Factura>> coleccion = CollectionModel.of(
                toModelList(facturaService.listarFacturas()),
                linkTo(methodOn(FacturaController.class).listarFacturas()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Factura>> obtenerPorId(@PathVariable Long id) {
        return facturaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<CollectionModel<EntityModel<Factura>>> obtenerPorPedido(@PathVariable Long idPedido) {
        CollectionModel<EntityModel<Factura>> coleccion = CollectionModel.of(
                toModelList(facturaService.findByPedido(idPedido)),
                linkTo(methodOn(FacturaController.class).obtenerPorPedido(idPedido)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Factura>> crearFactura(@RequestBody Factura factura) {
        try {
            Factura nuevo = facturaService.guardarFactura(factura);
            return ResponseEntity
                    .created(linkTo(methodOn(FacturaController.class).obtenerPorId(nuevo.getIdDocumentoTributario())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Factura>> actualizarFactura(@PathVariable Long id, @RequestBody Factura factura) {
        try {
            Factura actualizado = facturaService.actualizarFactura(id, factura);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) {
        facturaService.eliminarFactura(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<Factura>> toModelList(List<Factura> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(FacturaController.class).obtenerPorId(x.getIdDocumentoTributario())).withSelfRel()))
                .toList();
    }

    private EntityModel<Factura> toModel(Factura factura) {
        return EntityModel.of(factura,
                linkTo(methodOn(FacturaController.class).obtenerPorId(factura.getIdDocumentoTributario())).withSelfRel(),
                linkTo(methodOn(FacturaController.class).listarFacturas()).withRel("facturas"));
    }
}
