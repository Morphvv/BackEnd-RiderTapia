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

import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.service.CategoriaService;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Categoria>>> listarCategorias() {
        CollectionModel<EntityModel<Categoria>> coleccion = CollectionModel.of(
                toModelList(categoriaService.listarCategorias()),
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Categoria>> obtenerPorId(@PathVariable Long id) {
        return categoriaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<EntityModel<Categoria>> obtenerPorNombre(@PathVariable String nombre) {
        Categoria categoria = categoriaService.findByNombre(nombre);
        if (categoria == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toModel(categoria));
    }

    @PostMapping
    public ResponseEntity<EntityModel<Categoria>> crearCategoria(@RequestBody Categoria categoria) {
        try {
            Categoria nuevo = categoriaService.guardarCategoria(categoria);
            return ResponseEntity
                    .created(linkTo(methodOn(CategoriaController.class).obtenerPorId(nuevo.getIdCategoria())).toUri())
                    .body(toModel(nuevo));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Categoria>> actualizarCategoria(@PathVariable Long id, @RequestBody Categoria categoria) {
        try {
            Categoria actualizado = categoriaService.actualizarCategoria(id, categoria);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        try {
            categoriaService.eliminarCategoria(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private List<EntityModel<Categoria>> toModelList(List<Categoria> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(CategoriaController.class).obtenerPorId(x.getIdCategoria())).withSelfRel()))
                .toList();
    }

    private EntityModel<Categoria> toModel(Categoria categoria) {
        return EntityModel.of(categoria,
                linkTo(methodOn(CategoriaController.class).obtenerPorId(categoria.getIdCategoria())).withSelfRel(),
                linkTo(methodOn(CategoriaController.class).listarCategorias()).withRel("categorias"));
    }
}
