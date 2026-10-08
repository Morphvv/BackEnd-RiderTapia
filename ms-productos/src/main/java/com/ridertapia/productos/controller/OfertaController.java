package com.ridertapia.productos.controller;

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

import com.ridertapia.productos.model.Oferta;
import com.ridertapia.productos.service.OfertaService;

@RestController
@RequestMapping("/api/v1/ofertas")
public class OfertaController {

    @Autowired
    private OfertaService ofertaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Oferta>>> listarOfertas() {
        CollectionModel<EntityModel<Oferta>> coleccion = CollectionModel.of(
                toModelList(ofertaService.listarOfertas()),
                linkTo(methodOn(OfertaController.class).listarOfertas()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Oferta>> obtenerPorId(@PathVariable Long id) {
        return ofertaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/producto/{idProducto}")
    public ResponseEntity<CollectionModel<EntityModel<Oferta>>> obtenerPorProducto(@PathVariable Long idProducto) {
        CollectionModel<EntityModel<Oferta>> coleccion = CollectionModel.of(
                toModelList(ofertaService.findByProducto(idProducto)),
                linkTo(methodOn(OfertaController.class).obtenerPorProducto(idProducto)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Oferta>> crearOferta(@RequestBody Oferta oferta) {
        try {
            Oferta nuevo = ofertaService.guardarOferta(oferta);
            return ResponseEntity
                    .created(linkTo(methodOn(OfertaController.class).obtenerPorId(nuevo.getIdOferta())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Oferta>> actualizarOferta(@PathVariable Long id, @RequestBody Oferta oferta) {
        try {
            Oferta actualizado = ofertaService.actualizarOferta(id, oferta);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOferta(@PathVariable Long id) {
        ofertaService.eliminarOferta(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<Oferta>> toModelList(List<Oferta> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(OfertaController.class).obtenerPorId(x.getIdOferta())).withSelfRel()))
                .toList();
    }

    private EntityModel<Oferta> toModel(Oferta oferta) {
        return EntityModel.of(oferta,
                linkTo(methodOn(OfertaController.class).obtenerPorId(oferta.getIdOferta())).withSelfRel(),
                linkTo(methodOn(OfertaController.class).listarOfertas()).withRel("ofertas"));
    }
}
