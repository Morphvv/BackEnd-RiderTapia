package com.ridertapia.carrito.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.ridertapia.carrito.model.ItemCarrito;
import com.ridertapia.carrito.service.ItemCarritoService;

@RestController
@RequestMapping("/api/v1/items-carrito")
public class ItemCarritoController {

    @Autowired
    private ItemCarritoService itemCarritoService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ItemCarrito>>> listarItemsCarrito() {
        CollectionModel<EntityModel<ItemCarrito>> coleccion = CollectionModel.of(
                toModelList(itemCarritoService.listarItemsCarrito()),
                linkTo(methodOn(ItemCarritoController.class).listarItemsCarrito()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ItemCarrito>> obtenerPorId(@PathVariable Long id) {
        return itemCarritoService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/carrito/{idCarrito}")
    public ResponseEntity<CollectionModel<EntityModel<ItemCarrito>>> obtenerPorCarrito(@PathVariable Long idCarrito) {
        CollectionModel<EntityModel<ItemCarrito>> coleccion = CollectionModel.of(
                toModelList(itemCarritoService.findByCarrito(idCarrito)),
                linkTo(methodOn(ItemCarritoController.class).obtenerPorCarrito(idCarrito)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<ItemCarrito>> crearItemCarrito(@RequestBody ItemCarrito itemCarrito) {
        try {
            ItemCarrito nuevo = itemCarritoService.guardarItemCarrito(itemCarrito);
            return ResponseEntity
                    .created(linkTo(methodOn(ItemCarritoController.class).obtenerPorId(nuevo.getIdItemCarrito())).toUri())
                    .body(toModel(nuevo));
        } catch (IllegalStateException e) {
            // Stock insuficiente en ms-inventario
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ItemCarrito>> actualizarItemCarrito(@PathVariable Long id, @RequestBody ItemCarrito itemCarrito) {
        try {
            ItemCarrito actualizado = itemCarritoService.actualizarItemCarrito(id, itemCarrito);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (IllegalStateException e) {
            // Stock insuficiente en ms-inventario
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItemCarrito(@PathVariable Long id) {
        itemCarritoService.eliminarItemCarrito(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<ItemCarrito>> toModelList(List<ItemCarrito> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(ItemCarritoController.class).obtenerPorId(x.getIdItemCarrito())).withSelfRel()))
                .toList();
    }

    private EntityModel<ItemCarrito> toModel(ItemCarrito itemCarrito) {
        return EntityModel.of(itemCarrito,
                linkTo(methodOn(ItemCarritoController.class).obtenerPorId(itemCarrito.getIdItemCarrito())).withSelfRel(),
                linkTo(methodOn(ItemCarritoController.class).listarItemsCarrito()).withRel("itemsCarrito"));
    }
}
