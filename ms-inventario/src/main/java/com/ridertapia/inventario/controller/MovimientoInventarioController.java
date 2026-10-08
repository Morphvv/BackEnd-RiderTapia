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

import com.ridertapia.inventario.model.MovimientoInventario;
import com.ridertapia.inventario.service.MovimientoInventarioService;

@RestController
@RequestMapping("/api/v1/movimientos")
public class MovimientoInventarioController {

    @Autowired
    private MovimientoInventarioService movimientoInventarioService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<MovimientoInventario>>> listarMovimientosInventario() {
        CollectionModel<EntityModel<MovimientoInventario>> coleccion = CollectionModel.of(
                toModelList(movimientoInventarioService.listarMovimientosInventario()),
                linkTo(methodOn(MovimientoInventarioController.class).listarMovimientosInventario()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<MovimientoInventario>> obtenerPorId(@PathVariable Long id) {
        return movimientoInventarioService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/itemInventario/{idItemInventario}")
    public ResponseEntity<CollectionModel<EntityModel<MovimientoInventario>>> obtenerPorItemInventario(@PathVariable Long idItemInventario) {
        CollectionModel<EntityModel<MovimientoInventario>> coleccion = CollectionModel.of(
                toModelList(movimientoInventarioService.findByItemInventario(idItemInventario)),
                linkTo(methodOn(MovimientoInventarioController.class).obtenerPorItemInventario(idItemInventario)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<MovimientoInventario>> crearMovimientoInventario(@RequestBody MovimientoInventario movimientoInventario) {
        try {
            MovimientoInventario nuevo = movimientoInventarioService.guardarMovimientoInventario(movimientoInventario);
            return ResponseEntity
                    .created(linkTo(methodOn(MovimientoInventarioController.class).obtenerPorId(nuevo.getIdMovimientoInventario())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<MovimientoInventario>> actualizarMovimientoInventario(@PathVariable Long id, @RequestBody MovimientoInventario movimientoInventario) {
        try {
            MovimientoInventario actualizado = movimientoInventarioService.actualizarMovimientoInventario(id, movimientoInventario);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMovimientoInventario(@PathVariable Long id) {
        movimientoInventarioService.eliminarMovimientoInventario(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<MovimientoInventario>> toModelList(List<MovimientoInventario> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(MovimientoInventarioController.class).obtenerPorId(x.getIdMovimientoInventario())).withSelfRel()))
                .toList();
    }

    private EntityModel<MovimientoInventario> toModel(MovimientoInventario movimientoInventario) {
        return EntityModel.of(movimientoInventario,
                linkTo(methodOn(MovimientoInventarioController.class).obtenerPorId(movimientoInventario.getIdMovimientoInventario())).withSelfRel(),
                linkTo(methodOn(MovimientoInventarioController.class).listarMovimientosInventario()).withRel("movimientos"));
    }
}
