package com.ridertapia.pedidos.controller;

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

import com.ridertapia.pedidos.model.Boleta;
import com.ridertapia.pedidos.service.BoletaService;

@RestController
@RequestMapping("/api/v1/boletas")
public class BoletaController {

    @Autowired
    private BoletaService boletaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Boleta>>> listarBoletas() {
        CollectionModel<EntityModel<Boleta>> coleccion = CollectionModel.of(
                toModelList(boletaService.listarBoletas()),
                linkTo(methodOn(BoletaController.class).listarBoletas()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Boleta>> obtenerPorId(@PathVariable Long id) {
        return boletaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<CollectionModel<EntityModel<Boleta>>> obtenerPorPedido(@PathVariable Long idPedido) {
        CollectionModel<EntityModel<Boleta>> coleccion = CollectionModel.of(
                toModelList(boletaService.findByPedido(idPedido)),
                linkTo(methodOn(BoletaController.class).obtenerPorPedido(idPedido)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Boleta>> crearBoleta(@RequestBody Boleta boleta) {
        try {
            Boleta nuevo = boletaService.guardarBoleta(boleta);
            return ResponseEntity
                    .created(linkTo(methodOn(BoletaController.class).obtenerPorId(nuevo.getIdDocumentoTributario())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Boleta>> actualizarBoleta(@PathVariable Long id, @RequestBody Boleta boleta) {
        try {
            Boleta actualizado = boletaService.actualizarBoleta(id, boleta);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarBoleta(@PathVariable Long id) {
        boletaService.eliminarBoleta(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<Boleta>> toModelList(List<Boleta> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(BoletaController.class).obtenerPorId(x.getIdDocumentoTributario())).withSelfRel()))
                .toList();
    }

    private EntityModel<Boleta> toModel(Boleta boleta) {
        return EntityModel.of(boleta,
                linkTo(methodOn(BoletaController.class).obtenerPorId(boleta.getIdDocumentoTributario())).withSelfRel(),
                linkTo(methodOn(BoletaController.class).listarBoletas()).withRel("boletas"));
    }
}
