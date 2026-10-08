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

import com.ridertapia.inventario.model.CargaMasiva;
import com.ridertapia.inventario.service.CargaMasivaService;

@RestController
@RequestMapping("/api/v1/cargas-masivas")
public class CargaMasivaController {

    @Autowired
    private CargaMasivaService cargaMasivaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<CargaMasiva>>> listarCargasMasivas() {
        CollectionModel<EntityModel<CargaMasiva>> coleccion = CollectionModel.of(
                toModelList(cargaMasivaService.listarCargasMasivas()),
                linkTo(methodOn(CargaMasivaController.class).listarCargasMasivas()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<CargaMasiva>> obtenerPorId(@PathVariable Long id) {
        return cargaMasivaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/idEmpleado/{idEmpleado}")
    public ResponseEntity<CollectionModel<EntityModel<CargaMasiva>>> obtenerPorIdEmpleado(@PathVariable Long idEmpleado) {
        CollectionModel<EntityModel<CargaMasiva>> coleccion = CollectionModel.of(
                toModelList(cargaMasivaService.findByIdEmpleado(idEmpleado)),
                linkTo(methodOn(CargaMasivaController.class).obtenerPorIdEmpleado(idEmpleado)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<CargaMasiva>> crearCargaMasiva(@RequestBody CargaMasiva cargaMasiva) {
        CargaMasiva nuevo = cargaMasivaService.guardarCargaMasiva(cargaMasiva);
        return ResponseEntity
                .created(linkTo(methodOn(CargaMasivaController.class).obtenerPorId(nuevo.getIdCargaMasiva())).toUri())
                .body(toModel(nuevo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<CargaMasiva>> actualizarCargaMasiva(@PathVariable Long id, @RequestBody CargaMasiva cargaMasiva) {
        try {
            CargaMasiva actualizado = cargaMasivaService.actualizarCargaMasiva(id, cargaMasiva);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCargaMasiva(@PathVariable Long id) {
        try {
            cargaMasivaService.eliminarCargaMasiva(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private List<EntityModel<CargaMasiva>> toModelList(List<CargaMasiva> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(CargaMasivaController.class).obtenerPorId(x.getIdCargaMasiva())).withSelfRel()))
                .toList();
    }

    private EntityModel<CargaMasiva> toModel(CargaMasiva cargaMasiva) {
        return EntityModel.of(cargaMasiva,
                linkTo(methodOn(CargaMasivaController.class).obtenerPorId(cargaMasiva.getIdCargaMasiva())).withSelfRel(),
                linkTo(methodOn(CargaMasivaController.class).listarCargasMasivas()).withRel("cargasMasivas"));
    }
}
