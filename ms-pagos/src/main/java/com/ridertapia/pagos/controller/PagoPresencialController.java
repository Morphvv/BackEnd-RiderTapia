package com.ridertapia.pagos.controller;

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

import com.ridertapia.pagos.model.PagoPresencial;
import com.ridertapia.pagos.service.PagoPresencialService;

@RestController
@RequestMapping("/api/v1/pagos-presenciales")
public class PagoPresencialController {

    @Autowired
    private PagoPresencialService pagoPresencialService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PagoPresencial>>> listarPagosPresenciales() {
        CollectionModel<EntityModel<PagoPresencial>> coleccion = CollectionModel.of(
                toModelList(pagoPresencialService.listarPagosPresenciales()),
                linkTo(methodOn(PagoPresencialController.class).listarPagosPresenciales()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PagoPresencial>> obtenerPorId(@PathVariable Long id) {
        return pagoPresencialService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/idPedido/{idPedido}")
    public ResponseEntity<CollectionModel<EntityModel<PagoPresencial>>> obtenerPorIdPedido(@PathVariable Long idPedido) {
        CollectionModel<EntityModel<PagoPresencial>> coleccion = CollectionModel.of(
                toModelList(pagoPresencialService.findByIdPedido(idPedido)),
                linkTo(methodOn(PagoPresencialController.class).obtenerPorIdPedido(idPedido)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<PagoPresencial>> crearPagoPresencial(@RequestBody PagoPresencial pagoPresencial) {
        try {
            PagoPresencial nuevo = pagoPresencialService.guardarPagoPresencial(pagoPresencial);
            return ResponseEntity
                    .created(linkTo(methodOn(PagoPresencialController.class).obtenerPorId(nuevo.getIdPago())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<PagoPresencial>> actualizarPagoPresencial(@PathVariable Long id, @RequestBody PagoPresencial pagoPresencial) {
        try {
            PagoPresencial actualizado = pagoPresencialService.actualizarPagoPresencial(id, pagoPresencial);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPagoPresencial(@PathVariable Long id) {
        pagoPresencialService.eliminarPagoPresencial(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<PagoPresencial>> toModelList(List<PagoPresencial> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(PagoPresencialController.class).obtenerPorId(x.getIdPago())).withSelfRel()))
                .toList();
    }

    private EntityModel<PagoPresencial> toModel(PagoPresencial pagoPresencial) {
        return EntityModel.of(pagoPresencial,
                linkTo(methodOn(PagoPresencialController.class).obtenerPorId(pagoPresencial.getIdPago())).withSelfRel(),
                linkTo(methodOn(PagoPresencialController.class).listarPagosPresenciales()).withRel("pagosPresenciales"));
    }
}
