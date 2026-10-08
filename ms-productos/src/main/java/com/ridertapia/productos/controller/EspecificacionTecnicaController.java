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

import com.ridertapia.productos.model.EspecificacionTecnica;
import com.ridertapia.productos.service.EspecificacionTecnicaService;

@RestController
@RequestMapping("/api/v1/especificaciones")
public class EspecificacionTecnicaController {

    @Autowired
    private EspecificacionTecnicaService especificacionTecnicaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<EspecificacionTecnica>>> listarEspecificacionesTecnicas() {
        CollectionModel<EntityModel<EspecificacionTecnica>> coleccion = CollectionModel.of(
                toModelList(especificacionTecnicaService.listarEspecificacionesTecnicas()),
                linkTo(methodOn(EspecificacionTecnicaController.class).listarEspecificacionesTecnicas()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<EspecificacionTecnica>> obtenerPorId(@PathVariable Long id) {
        return especificacionTecnicaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<CollectionModel<EntityModel<EspecificacionTecnica>>> obtenerPorProducto(@PathVariable Long idProducto) {
        CollectionModel<EntityModel<EspecificacionTecnica>> coleccion = CollectionModel.of(
                toModelList(especificacionTecnicaService.findByProducto(idProducto)),
                linkTo(methodOn(EspecificacionTecnicaController.class).obtenerPorProducto(idProducto)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<EspecificacionTecnica>> crearEspecificacionTecnica(@RequestBody EspecificacionTecnica especificacionTecnica) {
        try {
            EspecificacionTecnica nuevo = especificacionTecnicaService.guardarEspecificacionTecnica(especificacionTecnica);
            return ResponseEntity
                    .created(linkTo(methodOn(EspecificacionTecnicaController.class).obtenerPorId(nuevo.getIdEspecificacionTecnica())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<EspecificacionTecnica>> actualizarEspecificacionTecnica(@PathVariable Long id, @RequestBody EspecificacionTecnica especificacionTecnica) {
        try {
            EspecificacionTecnica actualizado = especificacionTecnicaService.actualizarEspecificacionTecnica(id, especificacionTecnica);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEspecificacionTecnica(@PathVariable Long id) {
        especificacionTecnicaService.eliminarEspecificacionTecnica(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<EspecificacionTecnica>> toModelList(List<EspecificacionTecnica> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(EspecificacionTecnicaController.class).obtenerPorId(x.getIdEspecificacionTecnica())).withSelfRel()))
                .toList();
    }

    private EntityModel<EspecificacionTecnica> toModel(EspecificacionTecnica especificacionTecnica) {
        return EntityModel.of(especificacionTecnica,
                linkTo(methodOn(EspecificacionTecnicaController.class).obtenerPorId(especificacionTecnica.getIdEspecificacionTecnica())).withSelfRel(),
                linkTo(methodOn(EspecificacionTecnicaController.class).listarEspecificacionesTecnicas()).withRel("especificaciones"));
    }
}
