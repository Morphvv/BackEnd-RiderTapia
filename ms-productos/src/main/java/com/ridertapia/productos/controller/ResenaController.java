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

import com.ridertapia.productos.model.Resena;
import com.ridertapia.productos.service.ResenaService;

@RestController
@RequestMapping("/api/v1/resenas")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Resena>>> listarResenas() {
        CollectionModel<EntityModel<Resena>> coleccion = CollectionModel.of(
                toModelList(resenaService.listarResenas()),
                linkTo(methodOn(ResenaController.class).listarResenas()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Resena>> obtenerPorId(@PathVariable Long id) {
        return resenaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<CollectionModel<EntityModel<Resena>>> obtenerPorProducto(@PathVariable Long idProducto) {
        CollectionModel<EntityModel<Resena>> coleccion = CollectionModel.of(
                toModelList(resenaService.findByProducto(idProducto)),
                linkTo(methodOn(ResenaController.class).obtenerPorProducto(idProducto)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Resena>> crearResena(@RequestBody Resena resena) {
        try {
            Resena nuevo = resenaService.guardarResena(resena);
            return ResponseEntity
                    .created(linkTo(methodOn(ResenaController.class).obtenerPorId(nuevo.getIdResena())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Resena>> actualizarResena(@PathVariable Long id, @RequestBody Resena resena) {
        try {
            Resena actualizado = resenaService.actualizarResena(id, resena);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarResena(@PathVariable Long id) {
        resenaService.eliminarResena(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<Resena>> toModelList(List<Resena> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(ResenaController.class).obtenerPorId(x.getIdResena())).withSelfRel()))
                .toList();
    }

    private EntityModel<Resena> toModel(Resena resena) {
        return EntityModel.of(resena,
                linkTo(methodOn(ResenaController.class).obtenerPorId(resena.getIdResena())).withSelfRel(),
                linkTo(methodOn(ResenaController.class).listarResenas()).withRel("resenas"));
    }
}
