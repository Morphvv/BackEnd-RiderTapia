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

import com.ridertapia.pedidos.model.HistorialEstadoPedido;
import com.ridertapia.pedidos.service.HistorialEstadoPedidoService;

@RestController
@RequestMapping("/api/v1/historial-estados")
public class HistorialEstadoPedidoController {

    @Autowired
    private HistorialEstadoPedidoService historialEstadoPedidoService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<HistorialEstadoPedido>>> listarHistorialesEstadoPedido() {
        CollectionModel<EntityModel<HistorialEstadoPedido>> coleccion = CollectionModel.of(
                toModelList(historialEstadoPedidoService.listarHistorialesEstadoPedido()),
                linkTo(methodOn(HistorialEstadoPedidoController.class).listarHistorialesEstadoPedido()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<HistorialEstadoPedido>> obtenerPorId(@PathVariable Long id) {
        return historialEstadoPedidoService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<CollectionModel<EntityModel<HistorialEstadoPedido>>> obtenerPorPedido(@PathVariable Long idPedido) {
        CollectionModel<EntityModel<HistorialEstadoPedido>> coleccion = CollectionModel.of(
                toModelList(historialEstadoPedidoService.findByPedido(idPedido)),
                linkTo(methodOn(HistorialEstadoPedidoController.class).obtenerPorPedido(idPedido)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<HistorialEstadoPedido>> crearHistorialEstadoPedido(@RequestBody HistorialEstadoPedido historialEstadoPedido) {
        try {
            HistorialEstadoPedido nuevo = historialEstadoPedidoService.guardarHistorialEstadoPedido(historialEstadoPedido);
            return ResponseEntity
                    .created(linkTo(methodOn(HistorialEstadoPedidoController.class).obtenerPorId(nuevo.getIdHistorialEstadoPedido())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<HistorialEstadoPedido>> actualizarHistorialEstadoPedido(@PathVariable Long id, @RequestBody HistorialEstadoPedido historialEstadoPedido) {
        try {
            HistorialEstadoPedido actualizado = historialEstadoPedidoService.actualizarHistorialEstadoPedido(id, historialEstadoPedido);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHistorialEstadoPedido(@PathVariable Long id) {
        historialEstadoPedidoService.eliminarHistorialEstadoPedido(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<HistorialEstadoPedido>> toModelList(List<HistorialEstadoPedido> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(HistorialEstadoPedidoController.class).obtenerPorId(x.getIdHistorialEstadoPedido())).withSelfRel()))
                .toList();
    }

    private EntityModel<HistorialEstadoPedido> toModel(HistorialEstadoPedido historialEstadoPedido) {
        return EntityModel.of(historialEstadoPedido,
                linkTo(methodOn(HistorialEstadoPedidoController.class).obtenerPorId(historialEstadoPedido.getIdHistorialEstadoPedido())).withSelfRel(),
                linkTo(methodOn(HistorialEstadoPedidoController.class).listarHistorialesEstadoPedido()).withRel("historialEstados"));
    }
}
