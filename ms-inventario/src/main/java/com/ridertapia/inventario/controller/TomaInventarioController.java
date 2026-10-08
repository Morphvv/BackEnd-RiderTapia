package com.ridertapia.inventario.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ridertapia.inventario.model.TomaInventario;
import com.ridertapia.inventario.service.TomaInventarioService;

@RestController
@RequestMapping("/api/v1/tomas-inventario")
public class TomaInventarioController {

    @Autowired
    private TomaInventarioService tomaInventarioService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<TomaInventario>>> listarTomasInventario() {
        CollectionModel<EntityModel<TomaInventario>> coleccion = CollectionModel.of(
                toModelList(tomaInventarioService.listarTomasInventario()),
                linkTo(methodOn(TomaInventarioController.class).listarTomasInventario()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<TomaInventario>> obtenerPorId(@PathVariable Long id) {
        return tomaInventarioService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/idEmpleado/{idEmpleado}")
    public ResponseEntity<CollectionModel<EntityModel<TomaInventario>>> obtenerPorIdEmpleado(@PathVariable Long idEmpleado) {
        CollectionModel<EntityModel<TomaInventario>> coleccion = CollectionModel.of(
                toModelList(tomaInventarioService.findByIdEmpleado(idEmpleado)),
                linkTo(methodOn(TomaInventarioController.class).obtenerPorIdEmpleado(idEmpleado)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<TomaInventario>> crearTomaInventario(@RequestBody TomaInventario tomaInventario) {
        TomaInventario nuevo = tomaInventarioService.guardarTomaInventario(tomaInventario);
        return ResponseEntity
                .created(linkTo(methodOn(TomaInventarioController.class).obtenerPorId(nuevo.getIdTomaInventario())).toUri())
                .body(toModel(nuevo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<TomaInventario>> actualizarTomaInventario(@PathVariable Long id, @RequestBody TomaInventario tomaInventario) {
        try {
            TomaInventario actualizado = tomaInventarioService.actualizarTomaInventario(id, tomaInventario);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTomaInventario(@PathVariable Long id) {
        try {
            tomaInventarioService.eliminarTomaInventario(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private List<EntityModel<TomaInventario>> toModelList(List<TomaInventario> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(TomaInventarioController.class).obtenerPorId(x.getIdTomaInventario())).withSelfRel()))
                .toList();
    }

    private EntityModel<TomaInventario> toModel(TomaInventario tomaInventario) {
        return EntityModel.of(tomaInventario,
                linkTo(methodOn(TomaInventarioController.class).obtenerPorId(tomaInventario.getIdTomaInventario())).withSelfRel(),
                linkTo(methodOn(TomaInventarioController.class).listarTomasInventario()).withRel("tomasInventario"));
    }
}
