package com.ridertapia.inventario.controller;

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

import com.ridertapia.inventario.model.AlertaStock;
import com.ridertapia.inventario.service.AlertaStockService;

@RestController
@RequestMapping("/api/v1/alertas-stock")
public class AlertaStockController {

    @Autowired
    private AlertaStockService alertaStockService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AlertaStock>>> listarAlertasStock() {
        CollectionModel<EntityModel<AlertaStock>> coleccion = CollectionModel.of(
                toModelList(alertaStockService.listarAlertasStock()),
                linkTo(methodOn(AlertaStockController.class).listarAlertasStock()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AlertaStock>> obtenerPorId(@PathVariable Long id) {
        return alertaStockService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/itemInventario/{idItemInventario}")
    public ResponseEntity<CollectionModel<EntityModel<AlertaStock>>> obtenerPorItemInventario(@PathVariable Long idItemInventario) {
        CollectionModel<EntityModel<AlertaStock>> coleccion = CollectionModel.of(
                toModelList(alertaStockService.findByItemInventario(idItemInventario)),
                linkTo(methodOn(AlertaStockController.class).obtenerPorItemInventario(idItemInventario)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<AlertaStock>> crearAlertaStock(@RequestBody AlertaStock alertaStock) {
        try {
            AlertaStock nuevo = alertaStockService.guardarAlertaStock(alertaStock);
            return ResponseEntity
                    .created(linkTo(methodOn(AlertaStockController.class).obtenerPorId(nuevo.getIdAlertaStock())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<AlertaStock>> actualizarAlertaStock(@PathVariable Long id, @RequestBody AlertaStock alertaStock) {
        try {
            AlertaStock actualizado = alertaStockService.actualizarAlertaStock(id, alertaStock);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAlertaStock(@PathVariable Long id) {
        alertaStockService.eliminarAlertaStock(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<AlertaStock>> toModelList(List<AlertaStock> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(AlertaStockController.class).obtenerPorId(x.getIdAlertaStock())).withSelfRel()))
                .toList();
    }

    private EntityModel<AlertaStock> toModel(AlertaStock alertaStock) {
        return EntityModel.of(alertaStock,
                linkTo(methodOn(AlertaStockController.class).obtenerPorId(alertaStock.getIdAlertaStock())).withSelfRel(),
                linkTo(methodOn(AlertaStockController.class).listarAlertasStock()).withRel("alertasStock"));
    }
}
