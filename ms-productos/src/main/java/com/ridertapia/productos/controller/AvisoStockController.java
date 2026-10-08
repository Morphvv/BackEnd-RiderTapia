package com.ridertapia.productos.controller;

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

import com.ridertapia.productos.model.AvisoStock;
import com.ridertapia.productos.service.AvisoStockService;

@RestController
@RequestMapping("/api/v1/avisos-stock")
public class AvisoStockController {

    @Autowired
    private AvisoStockService avisoStockService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AvisoStock>>> listarAvisosStock() {
        CollectionModel<EntityModel<AvisoStock>> coleccion = CollectionModel.of(
                toModelList(avisoStockService.listarAvisosStock()),
                linkTo(methodOn(AvisoStockController.class).listarAvisosStock()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AvisoStock>> obtenerPorId(@PathVariable Long id) {
        return avisoStockService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<CollectionModel<EntityModel<AvisoStock>>> obtenerPorProducto(@PathVariable Long idProducto) {
        CollectionModel<EntityModel<AvisoStock>> coleccion = CollectionModel.of(
                toModelList(avisoStockService.findByProducto(idProducto)),
                linkTo(methodOn(AvisoStockController.class).obtenerPorProducto(idProducto)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<AvisoStock>> crearAvisoStock(@RequestBody AvisoStock avisoStock) {
        try {
            AvisoStock nuevo = avisoStockService.guardarAvisoStock(avisoStock);
            return ResponseEntity
                    .created(linkTo(methodOn(AvisoStockController.class).obtenerPorId(nuevo.getIdAvisoStock())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<AvisoStock>> actualizarAvisoStock(@PathVariable Long id, @RequestBody AvisoStock avisoStock) {
        try {
            AvisoStock actualizado = avisoStockService.actualizarAvisoStock(id, avisoStock);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAvisoStock(@PathVariable Long id) {
        avisoStockService.eliminarAvisoStock(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<AvisoStock>> toModelList(List<AvisoStock> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(AvisoStockController.class).obtenerPorId(x.getIdAvisoStock())).withSelfRel()))
                .toList();
    }

    private EntityModel<AvisoStock> toModel(AvisoStock avisoStock) {
        return EntityModel.of(avisoStock,
                linkTo(methodOn(AvisoStockController.class).obtenerPorId(avisoStock.getIdAvisoStock())).withSelfRel(),
                linkTo(methodOn(AvisoStockController.class).listarAvisosStock()).withRel("avisosStock"));
    }
}
