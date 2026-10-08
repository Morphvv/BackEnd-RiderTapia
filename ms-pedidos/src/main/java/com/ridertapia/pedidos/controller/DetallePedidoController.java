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

import com.ridertapia.pedidos.model.DetallePedido;
import com.ridertapia.pedidos.service.DetallePedidoService;

@RestController
@RequestMapping("/api/v1/detalles")
public class DetallePedidoController {

    @Autowired
    private DetallePedidoService detallePedidoService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<DetallePedido>>> listarDetallesPedido() {
        CollectionModel<EntityModel<DetallePedido>> coleccion = CollectionModel.of(
                toModelList(detallePedidoService.listarDetallesPedido()),
                linkTo(methodOn(DetallePedidoController.class).listarDetallesPedido()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<DetallePedido>> obtenerPorId(@PathVariable Long id) {
        return detallePedidoService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<CollectionModel<EntityModel<DetallePedido>>> obtenerPorPedido(@PathVariable Long idPedido) {
        CollectionModel<EntityModel<DetallePedido>> coleccion = CollectionModel.of(
                toModelList(detallePedidoService.findByPedido(idPedido)),
                linkTo(methodOn(DetallePedidoController.class).obtenerPorPedido(idPedido)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<DetallePedido>> crearDetallePedido(@RequestBody DetallePedido detallePedido) {
        try {
            DetallePedido nuevo = detallePedidoService.guardarDetallePedido(detallePedido);
            return ResponseEntity
                    .created(linkTo(methodOn(DetallePedidoController.class).obtenerPorId(nuevo.getIdDetallePedido())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<DetallePedido>> actualizarDetallePedido(@PathVariable Long id, @RequestBody DetallePedido detallePedido) {
        try {
            DetallePedido actualizado = detallePedidoService.actualizarDetallePedido(id, detallePedido);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDetallePedido(@PathVariable Long id) {
        detallePedidoService.eliminarDetallePedido(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<DetallePedido>> toModelList(List<DetallePedido> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(DetallePedidoController.class).obtenerPorId(x.getIdDetallePedido())).withSelfRel()))
                .toList();
    }

    private EntityModel<DetallePedido> toModel(DetallePedido detallePedido) {
        return EntityModel.of(detallePedido,
                linkTo(methodOn(DetallePedidoController.class).obtenerPorId(detallePedido.getIdDetallePedido())).withSelfRel(),
                linkTo(methodOn(DetallePedidoController.class).listarDetallesPedido()).withRel("detalles"));
    }
}
