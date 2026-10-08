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

import com.ridertapia.inventario.model.IngresoMercancia;
import com.ridertapia.inventario.service.IngresoMercanciaService;

@RestController
@RequestMapping("/api/v1/ingresos")
public class IngresoMercanciaController {

    @Autowired
    private IngresoMercanciaService ingresoMercanciaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<IngresoMercancia>>> listarIngresosMercancia() {
        CollectionModel<EntityModel<IngresoMercancia>> coleccion = CollectionModel.of(
                toModelList(ingresoMercanciaService.listarIngresosMercancia()),
                linkTo(methodOn(IngresoMercanciaController.class).listarIngresosMercancia()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<IngresoMercancia>> obtenerPorId(@PathVariable Long id) {
        return ingresoMercanciaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/itemInventario/{idItemInventario}")
    public ResponseEntity<CollectionModel<EntityModel<IngresoMercancia>>> obtenerPorItemInventario(@PathVariable Long idItemInventario) {
        CollectionModel<EntityModel<IngresoMercancia>> coleccion = CollectionModel.of(
                toModelList(ingresoMercanciaService.findByItemInventario(idItemInventario)),
                linkTo(methodOn(IngresoMercanciaController.class).obtenerPorItemInventario(idItemInventario)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<IngresoMercancia>> crearIngresoMercancia(@RequestBody IngresoMercancia ingresoMercancia) {
        try {
            IngresoMercancia nuevo = ingresoMercanciaService.guardarIngresoMercancia(ingresoMercancia);
            return ResponseEntity
                    .created(linkTo(methodOn(IngresoMercanciaController.class).obtenerPorId(nuevo.getIdMovimientoInventario())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<IngresoMercancia>> actualizarIngresoMercancia(@PathVariable Long id, @RequestBody IngresoMercancia ingresoMercancia) {
        try {
            IngresoMercancia actualizado = ingresoMercanciaService.actualizarIngresoMercancia(id, ingresoMercancia);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarIngresoMercancia(@PathVariable Long id) {
        ingresoMercanciaService.eliminarIngresoMercancia(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<IngresoMercancia>> toModelList(List<IngresoMercancia> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(IngresoMercanciaController.class).obtenerPorId(x.getIdMovimientoInventario())).withSelfRel()))
                .toList();
    }

    private EntityModel<IngresoMercancia> toModel(IngresoMercancia ingresoMercancia) {
        return EntityModel.of(ingresoMercancia,
                linkTo(methodOn(IngresoMercanciaController.class).obtenerPorId(ingresoMercancia.getIdMovimientoInventario())).withSelfRel(),
                linkTo(methodOn(IngresoMercanciaController.class).listarIngresosMercancia()).withRel("ingresos"));
    }
}
