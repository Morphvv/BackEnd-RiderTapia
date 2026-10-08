package com.ridertapia.productos.controller;

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

import com.ridertapia.productos.model.Compatibilidad;
import com.ridertapia.productos.service.CompatibilidadService;

@RestController
@RequestMapping("/api/v1/compatibilidades")
public class CompatibilidadController {

    @Autowired
    private CompatibilidadService compatibilidadService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Compatibilidad>>> listarCompatibilidades() {
        CollectionModel<EntityModel<Compatibilidad>> coleccion = CollectionModel.of(
                toModelList(compatibilidadService.listarCompatibilidades()),
                linkTo(methodOn(CompatibilidadController.class).listarCompatibilidades()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Compatibilidad>> obtenerPorId(@PathVariable Long id) {
        return compatibilidadService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/tipoBicicleta/{tipoBicicleta}")
    public ResponseEntity<CollectionModel<EntityModel<Compatibilidad>>> obtenerPorTipoBicicleta(@PathVariable String tipoBicicleta) {
        CollectionModel<EntityModel<Compatibilidad>> coleccion = CollectionModel.of(
                toModelList(compatibilidadService.findByTipoBicicleta(tipoBicicleta)),
                linkTo(methodOn(CompatibilidadController.class).obtenerPorTipoBicicleta(tipoBicicleta)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Compatibilidad>> crearCompatibilidad(@RequestBody Compatibilidad compatibilidad) {
        Compatibilidad nuevo = compatibilidadService.guardarCompatibilidad(compatibilidad);
        return ResponseEntity
                .created(linkTo(methodOn(CompatibilidadController.class).obtenerPorId(nuevo.getIdCompatibilidad())).toUri())
                .body(toModel(nuevo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Compatibilidad>> actualizarCompatibilidad(@PathVariable Long id, @RequestBody Compatibilidad compatibilidad) {
        try {
            Compatibilidad actualizado = compatibilidadService.actualizarCompatibilidad(id, compatibilidad);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCompatibilidad(@PathVariable Long id) {
        try {
            compatibilidadService.eliminarCompatibilidad(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private List<EntityModel<Compatibilidad>> toModelList(List<Compatibilidad> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(CompatibilidadController.class).obtenerPorId(x.getIdCompatibilidad())).withSelfRel()))
                .toList();
    }

    private EntityModel<Compatibilidad> toModel(Compatibilidad compatibilidad) {
        return EntityModel.of(compatibilidad,
                linkTo(methodOn(CompatibilidadController.class).obtenerPorId(compatibilidad.getIdCompatibilidad())).withSelfRel(),
                linkTo(methodOn(CompatibilidadController.class).listarCompatibilidades()).withRel("compatibilidades"));
    }
}
