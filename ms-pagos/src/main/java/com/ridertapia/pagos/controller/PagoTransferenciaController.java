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

import com.ridertapia.pagos.model.PagoTransferencia;
import com.ridertapia.pagos.service.PagoTransferenciaService;

@RestController
@RequestMapping("/api/v1/pagos-transferencia")
public class PagoTransferenciaController {

    @Autowired
    private PagoTransferenciaService pagoTransferenciaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PagoTransferencia>>> listarPagosTransferencia() {
        CollectionModel<EntityModel<PagoTransferencia>> coleccion = CollectionModel.of(
                toModelList(pagoTransferenciaService.listarPagosTransferencia()),
                linkTo(methodOn(PagoTransferenciaController.class).listarPagosTransferencia()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PagoTransferencia>> obtenerPorId(@PathVariable Long id) {
        return pagoTransferenciaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/idPedido/{idPedido}")
    public ResponseEntity<CollectionModel<EntityModel<PagoTransferencia>>> obtenerPorIdPedido(@PathVariable Long idPedido) {
        CollectionModel<EntityModel<PagoTransferencia>> coleccion = CollectionModel.of(
                toModelList(pagoTransferenciaService.findByIdPedido(idPedido)),
                linkTo(methodOn(PagoTransferenciaController.class).obtenerPorIdPedido(idPedido)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<PagoTransferencia>> crearPagoTransferencia(@RequestBody PagoTransferencia pagoTransferencia) {
        try {
            PagoTransferencia nuevo = pagoTransferenciaService.guardarPagoTransferencia(pagoTransferencia);
            return ResponseEntity
                    .created(linkTo(methodOn(PagoTransferenciaController.class).obtenerPorId(nuevo.getIdPago())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<PagoTransferencia>> actualizarPagoTransferencia(@PathVariable Long id, @RequestBody PagoTransferencia pagoTransferencia) {
        try {
            PagoTransferencia actualizado = pagoTransferenciaService.actualizarPagoTransferencia(id, pagoTransferencia);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPagoTransferencia(@PathVariable Long id) {
        pagoTransferenciaService.eliminarPagoTransferencia(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<PagoTransferencia>> toModelList(List<PagoTransferencia> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(PagoTransferenciaController.class).obtenerPorId(x.getIdPago())).withSelfRel()))
                .toList();
    }

    private EntityModel<PagoTransferencia> toModel(PagoTransferencia pagoTransferencia) {
        return EntityModel.of(pagoTransferencia,
                linkTo(methodOn(PagoTransferenciaController.class).obtenerPorId(pagoTransferencia.getIdPago())).withSelfRel(),
                linkTo(methodOn(PagoTransferenciaController.class).listarPagosTransferencia()).withRel("pagosTransferencia"));
    }
}
