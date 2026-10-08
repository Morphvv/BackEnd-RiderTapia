package com.ridertapia.pedidos.controller;

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

import com.ridertapia.pedidos.model.DatosVisitante;
import com.ridertapia.pedidos.service.DatosVisitanteService;

@RestController
@RequestMapping("/api/v1/visitantes")
public class DatosVisitanteController {

    @Autowired
    private DatosVisitanteService datosVisitanteService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DatosVisitante>>> listarDatosVisitantes() {
        CollectionModel<EntityModel<DatosVisitante>> coleccion = CollectionModel.of(
                toModelList(datosVisitanteService.listarDatosVisitantes()),
                linkTo(methodOn(DatosVisitanteController.class).listarDatosVisitantes()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DatosVisitante>> obtenerPorId(@PathVariable Long id) {
        return datosVisitanteService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/correo/{correo}")
    public ResponseEntity<CollectionModel<EntityModel<DatosVisitante>>> obtenerPorCorreo(@PathVariable String correo) {
        CollectionModel<EntityModel<DatosVisitante>> coleccion = CollectionModel.of(
                toModelList(datosVisitanteService.findByCorreo(correo)),
                linkTo(methodOn(DatosVisitanteController.class).obtenerPorCorreo(correo)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<DatosVisitante>> crearDatosVisitante(@RequestBody DatosVisitante datosVisitante) {
        DatosVisitante nuevo = datosVisitanteService.guardarDatosVisitante(datosVisitante);
        return ResponseEntity
                .created(linkTo(methodOn(DatosVisitanteController.class).obtenerPorId(nuevo.getIdDatosVisitante())).toUri())
                .body(toModel(nuevo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<DatosVisitante>> actualizarDatosVisitante(@PathVariable Long id, @RequestBody DatosVisitante datosVisitante) {
        try {
            DatosVisitante actualizado = datosVisitanteService.actualizarDatosVisitante(id, datosVisitante);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDatosVisitante(@PathVariable Long id) {
        try {
            datosVisitanteService.eliminarDatosVisitante(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private List<EntityModel<DatosVisitante>> toModelList(List<DatosVisitante> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(DatosVisitanteController.class).obtenerPorId(x.getIdDatosVisitante())).withSelfRel()))
                .toList();
    }

    private EntityModel<DatosVisitante> toModel(DatosVisitante datosVisitante) {
        return EntityModel.of(datosVisitante,
                linkTo(methodOn(DatosVisitanteController.class).obtenerPorId(datosVisitante.getIdDatosVisitante())).withSelfRel(),
                linkTo(methodOn(DatosVisitanteController.class).listarDatosVisitantes()).withRel("visitantes"));
    }
}
