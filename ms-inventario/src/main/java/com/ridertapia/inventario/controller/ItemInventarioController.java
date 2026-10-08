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

import com.ridertapia.inventario.model.ItemInventario;
import com.ridertapia.inventario.service.ItemInventarioService;

@RestController
@RequestMapping("/api/v1/items-inventario")
public class ItemInventarioController {

    @Autowired
    private ItemInventarioService itemInventarioService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<ItemInventario>>> listarItemsInventario() {
        CollectionModel<EntityModel<ItemInventario>> coleccion = CollectionModel.of(
                toModelList(itemInventarioService.listarItemsInventario()),
                linkTo(methodOn(ItemInventarioController.class).listarItemsInventario()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<ItemInventario>> obtenerPorId(@PathVariable Long id) {
        return itemInventarioService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/idVariante/{idVariante}")
    public ResponseEntity<EntityModel<ItemInventario>> obtenerPorIdVariante(@PathVariable Long idVariante) {
        ItemInventario itemInventario = itemInventarioService.findByIdVariante(idVariante);
        if (itemInventario == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toModel(itemInventario));
    }

    @PostMapping
    public ResponseEntity<EntityModel<ItemInventario>> crearItemInventario(@RequestBody ItemInventario itemInventario) {
        try {
            ItemInventario nuevo = itemInventarioService.guardarItemInventario(itemInventario);
            return ResponseEntity
                    .created(linkTo(methodOn(ItemInventarioController.class).obtenerPorId(nuevo.getIdItemInventario())).toUri())
                    .body(toModel(nuevo));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<ItemInventario>> actualizarItemInventario(@PathVariable Long id, @RequestBody ItemInventario itemInventario) {
        try {
            ItemInventario actualizado = itemInventarioService.actualizarItemInventario(id, itemInventario);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItemInventario(@PathVariable Long id) {
        try {
            itemInventarioService.eliminarItemInventario(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private List<EntityModel<ItemInventario>> toModelList(List<ItemInventario> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(ItemInventarioController.class).obtenerPorId(x.getIdItemInventario())).withSelfRel()))
                .toList();
    }

    private EntityModel<ItemInventario> toModel(ItemInventario itemInventario) {
        return EntityModel.of(itemInventario,
                linkTo(methodOn(ItemInventarioController.class).obtenerPorId(itemInventario.getIdItemInventario())).withSelfRel(),
                linkTo(methodOn(ItemInventarioController.class).listarItemsInventario()).withRel("itemsInventario"));
    }
}
