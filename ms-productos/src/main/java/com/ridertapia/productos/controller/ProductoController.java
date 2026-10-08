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

import com.ridertapia.productos.model.Producto;
import com.ridertapia.productos.service.ProductoService;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> listarProductos() {
        CollectionModel<EntityModel<Producto>> coleccion = CollectionModel.of(
                toModelList(productoService.listarProductos()),
                linkTo(methodOn(ProductoController.class).listarProductos()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> obtenerPorId(@PathVariable Long id) {
        return productoService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categoria/{idCategoria}")
    public ResponseEntity<CollectionModel<EntityModel<Producto>>> obtenerPorCategoria(@PathVariable Long idCategoria) {
        CollectionModel<EntityModel<Producto>> coleccion = CollectionModel.of(
                toModelList(productoService.findByCategoria(idCategoria)),
                linkTo(methodOn(ProductoController.class).obtenerPorCategoria(idCategoria)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Producto>> crearProducto(@RequestBody Producto producto) {
        try {
            Producto nuevo = productoService.guardarProducto(producto);
            return ResponseEntity
                    .created(linkTo(methodOn(ProductoController.class).obtenerPorId(nuevo.getIdProducto())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Producto>> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        try {
            Producto actualizado = productoService.actualizarProducto(id, producto);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private List<EntityModel<Producto>> toModelList(List<Producto> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(ProductoController.class).obtenerPorId(x.getIdProducto())).withSelfRel()))
                .toList();
    }

    private EntityModel<Producto> toModel(Producto producto) {
        return EntityModel.of(producto,
                linkTo(methodOn(ProductoController.class).obtenerPorId(producto.getIdProducto())).withSelfRel(),
                linkTo(methodOn(ProductoController.class).listarProductos()).withRel("productos"));
    }
}
