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

import com.ridertapia.inventario.model.DetalleConteo;
import com.ridertapia.inventario.service.DetalleConteoService;

@RestController
@RequestMapping("/api/v1/detalles-conteo")
public class DetalleConteoController {

    @Autowired
    private DetalleConteoService detalleConteoService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DetalleConteo>>> listarDetallesConteo() {
        CollectionModel<EntityModel<DetalleConteo>> coleccion = CollectionModel.of(
                toModelList(detalleConteoService.listarDetallesConteo()),
                linkTo(methodOn(DetalleConteoController.class).listarDetallesConteo()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DetalleConteo>> obtenerPorId(@PathVariable Long id) {
        return detalleConteoService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tomaInventario/{idTomaInventario}")
    public ResponseEntity<CollectionModel<EntityModel<DetalleConteo>>> obtenerPorTomaInventario(@PathVariable Long idTomaInventario) {
        CollectionModel<EntityModel<DetalleConteo>> coleccion = CollectionModel.of(
                toModelList(detalleConteoService.findByTomaInventario(idTomaInventario)),
                linkTo(methodOn(DetalleConteoController.class).obtenerPorTomaInventario(idTomaInventario)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<DetalleConteo>> crearDetalleConteo(@RequestBody DetalleConteo detalleConteo) {
        try {
            DetalleConteo nuevo = detalleConteoService.guardarDetalleConteo(detalleConteo);
            return ResponseEntity
                    .created(linkTo(methodOn(DetalleConteoController.class).obtenerPorId(nuevo.getIdDetalleConteo())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<DetalleConteo>> actualizarDetalleConteo(@PathVariable Long id, @RequestBody DetalleConteo detalleConteo) {
        try {
            DetalleConteo actualizado = detalleConteoService.actualizarDetalleConteo(id, detalleConteo);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDetalleConteo(@PathVariable Long id) {
        detalleConteoService.eliminarDetalleConteo(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<DetalleConteo>> toModelList(List<DetalleConteo> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(DetalleConteoController.class).obtenerPorId(x.getIdDetalleConteo())).withSelfRel()))
                .toList();
    }

    private EntityModel<DetalleConteo> toModel(DetalleConteo detalleConteo) {
        return EntityModel.of(detalleConteo,
                linkTo(methodOn(DetalleConteoController.class).obtenerPorId(detalleConteo.getIdDetalleConteo())).withSelfRel(),
                linkTo(methodOn(DetalleConteoController.class).listarDetallesConteo()).withRel("detallesConteo"));
    }
}
