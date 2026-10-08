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

import com.ridertapia.productos.model.Marca;
import com.ridertapia.productos.service.MarcaService;

@RestController
@RequestMapping("/api/v1/marcas")
public class MarcaController {

    @Autowired
    private MarcaService marcaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Marca>>> listarMarcas() {
        CollectionModel<EntityModel<Marca>> coleccion = CollectionModel.of(
                toModelList(marcaService.listarMarcas()),
                linkTo(methodOn(MarcaController.class).listarMarcas()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Marca>> obtenerPorId(@PathVariable Long id) {
        return marcaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<EntityModel<Marca>> obtenerPorNombre(@PathVariable String nombre) {
        Marca marca = marcaService.findByNombre(nombre);
        if (marca == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toModel(marca));
    }

    @PostMapping
    public ResponseEntity<EntityModel<Marca>> crearMarca(@RequestBody Marca marca) {
        try {
            Marca nuevo = marcaService.guardarMarca(marca);
            return ResponseEntity
                    .created(linkTo(methodOn(MarcaController.class).obtenerPorId(nuevo.getIdMarca())).toUri())
                    .body(toModel(nuevo));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Marca>> actualizarMarca(@PathVariable Long id, @RequestBody Marca marca) {
        try {
            Marca actualizado = marcaService.actualizarMarca(id, marca);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMarca(@PathVariable Long id) {
        try {
            marcaService.eliminarMarca(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private List<EntityModel<Marca>> toModelList(List<Marca> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(MarcaController.class).obtenerPorId(x.getIdMarca())).withSelfRel()))
                .toList();
    }

    private EntityModel<Marca> toModel(Marca marca) {
        return EntityModel.of(marca,
                linkTo(methodOn(MarcaController.class).obtenerPorId(marca.getIdMarca())).withSelfRel(),
                linkTo(methodOn(MarcaController.class).listarMarcas()).withRel("marcas"));
    }
}
