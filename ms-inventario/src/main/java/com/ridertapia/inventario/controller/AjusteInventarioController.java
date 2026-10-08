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

import com.ridertapia.inventario.model.AjusteInventario;
import com.ridertapia.inventario.service.AjusteInventarioService;

@RestController
@RequestMapping("/api/v1/ajustes")
public class AjusteInventarioController {

    @Autowired
    private AjusteInventarioService ajusteInventarioService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<AjusteInventario>>> listarAjustesInventario() {
        CollectionModel<EntityModel<AjusteInventario>> coleccion = CollectionModel.of(
                toModelList(ajusteInventarioService.listarAjustesInventario()),
                linkTo(methodOn(AjusteInventarioController.class).listarAjustesInventario()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<AjusteInventario>> obtenerPorId(@PathVariable Long id) {
        return ajusteInventarioService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/itemInventario/{idItemInventario}")
    public ResponseEntity<CollectionModel<EntityModel<AjusteInventario>>> obtenerPorItemInventario(@PathVariable Long idItemInventario) {
        CollectionModel<EntityModel<AjusteInventario>> coleccion = CollectionModel.of(
                toModelList(ajusteInventarioService.findByItemInventario(idItemInventario)),
                linkTo(methodOn(AjusteInventarioController.class).obtenerPorItemInventario(idItemInventario)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<AjusteInventario>> crearAjusteInventario(@RequestBody AjusteInventario ajusteInventario) {
        try {
            AjusteInventario nuevo = ajusteInventarioService.guardarAjusteInventario(ajusteInventario);
            return ResponseEntity
                    .created(linkTo(methodOn(AjusteInventarioController.class).obtenerPorId(nuevo.getIdMovimientoInventario())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<AjusteInventario>> actualizarAjusteInventario(@PathVariable Long id, @RequestBody AjusteInventario ajusteInventario) {
        try {
            AjusteInventario actualizado = ajusteInventarioService.actualizarAjusteInventario(id, ajusteInventario);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarAjusteInventario(@PathVariable Long id) {
        ajusteInventarioService.eliminarAjusteInventario(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<AjusteInventario>> toModelList(List<AjusteInventario> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(AjusteInventarioController.class).obtenerPorId(x.getIdMovimientoInventario())).withSelfRel()))
                .toList();
    }

    private EntityModel<AjusteInventario> toModel(AjusteInventario ajusteInventario) {
        return EntityModel.of(ajusteInventario,
                linkTo(methodOn(AjusteInventarioController.class).obtenerPorId(ajusteInventario.getIdMovimientoInventario())).withSelfRel(),
                linkTo(methodOn(AjusteInventarioController.class).listarAjustesInventario()).withRel("ajustes"));
    }
}
