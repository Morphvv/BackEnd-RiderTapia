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

import com.ridertapia.inventario.model.ErrorCargaMasiva;
import com.ridertapia.inventario.service.ErrorCargaMasivaService;

@RestController
@RequestMapping("/api/v1/errores-carga")
public class ErrorCargaMasivaController {

    @Autowired
    private ErrorCargaMasivaService errorCargaMasivaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ErrorCargaMasiva>>> listarErroresCargaMasiva() {
        CollectionModel<EntityModel<ErrorCargaMasiva>> coleccion = CollectionModel.of(
                toModelList(errorCargaMasivaService.listarErroresCargaMasiva()),
                linkTo(methodOn(ErrorCargaMasivaController.class).listarErroresCargaMasiva()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ErrorCargaMasiva>> obtenerPorId(@PathVariable Long id) {
        return errorCargaMasivaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cargaMasiva/{idCargaMasiva}")
    public ResponseEntity<CollectionModel<EntityModel<ErrorCargaMasiva>>> obtenerPorCargaMasiva(@PathVariable Long idCargaMasiva) {
        CollectionModel<EntityModel<ErrorCargaMasiva>> coleccion = CollectionModel.of(
                toModelList(errorCargaMasivaService.findByCargaMasiva(idCargaMasiva)),
                linkTo(methodOn(ErrorCargaMasivaController.class).obtenerPorCargaMasiva(idCargaMasiva)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<ErrorCargaMasiva>> crearErrorCargaMasiva(@RequestBody ErrorCargaMasiva errorCargaMasiva) {
        try {
            ErrorCargaMasiva nuevo = errorCargaMasivaService.guardarErrorCargaMasiva(errorCargaMasiva);
            return ResponseEntity
                    .created(linkTo(methodOn(ErrorCargaMasivaController.class).obtenerPorId(nuevo.getIdErrorCargaMasiva())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ErrorCargaMasiva>> actualizarErrorCargaMasiva(@PathVariable Long id, @RequestBody ErrorCargaMasiva errorCargaMasiva) {
        try {
            ErrorCargaMasiva actualizado = errorCargaMasivaService.actualizarErrorCargaMasiva(id, errorCargaMasiva);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarErrorCargaMasiva(@PathVariable Long id) {
        errorCargaMasivaService.eliminarErrorCargaMasiva(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<ErrorCargaMasiva>> toModelList(List<ErrorCargaMasiva> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(ErrorCargaMasivaController.class).obtenerPorId(x.getIdErrorCargaMasiva())).withSelfRel()))
                .toList();
    }

    private EntityModel<ErrorCargaMasiva> toModel(ErrorCargaMasiva errorCargaMasiva) {
        return EntityModel.of(errorCargaMasiva,
                linkTo(methodOn(ErrorCargaMasivaController.class).obtenerPorId(errorCargaMasiva.getIdErrorCargaMasiva())).withSelfRel(),
                linkTo(methodOn(ErrorCargaMasivaController.class).listarErroresCargaMasiva()).withRel("erroresCarga"));
    }
}
