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

import com.ridertapia.pedidos.model.NotificacionPedido;
import com.ridertapia.pedidos.service.NotificacionPedidoService;

@RestController
@RequestMapping("/api/v1/notificaciones")
public class NotificacionPedidoController {

    @Autowired
    private NotificacionPedidoService notificacionPedidoService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<NotificacionPedido>>> listarNotificacionesPedido() {
        CollectionModel<EntityModel<NotificacionPedido>> coleccion = CollectionModel.of(
                toModelList(notificacionPedidoService.listarNotificacionesPedido()),
                linkTo(methodOn(NotificacionPedidoController.class).listarNotificacionesPedido()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<NotificacionPedido>> obtenerPorId(@PathVariable Long id) {
        return notificacionPedidoService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{idPedido}")
    public ResponseEntity<CollectionModel<EntityModel<NotificacionPedido>>> obtenerPorPedido(@PathVariable Long idPedido) {
        CollectionModel<EntityModel<NotificacionPedido>> coleccion = CollectionModel.of(
                toModelList(notificacionPedidoService.findByPedido(idPedido)),
                linkTo(methodOn(NotificacionPedidoController.class).obtenerPorPedido(idPedido)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<NotificacionPedido>> crearNotificacionPedido(@RequestBody NotificacionPedido notificacionPedido) {
        try {
            NotificacionPedido nuevo = notificacionPedidoService.guardarNotificacionPedido(notificacionPedido);
            return ResponseEntity
                    .created(linkTo(methodOn(NotificacionPedidoController.class).obtenerPorId(nuevo.getIdNotificacionPedido())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<NotificacionPedido>> actualizarNotificacionPedido(@PathVariable Long id, @RequestBody NotificacionPedido notificacionPedido) {
        try {
            NotificacionPedido actualizado = notificacionPedidoService.actualizarNotificacionPedido(id, notificacionPedido);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarNotificacionPedido(@PathVariable Long id) {
        notificacionPedidoService.eliminarNotificacionPedido(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<NotificacionPedido>> toModelList(List<NotificacionPedido> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(NotificacionPedidoController.class).obtenerPorId(x.getIdNotificacionPedido())).withSelfRel()))
                .toList();
    }

    private EntityModel<NotificacionPedido> toModel(NotificacionPedido notificacionPedido) {
        return EntityModel.of(notificacionPedido,
                linkTo(methodOn(NotificacionPedidoController.class).obtenerPorId(notificacionPedido.getIdNotificacionPedido())).withSelfRel(),
                linkTo(methodOn(NotificacionPedidoController.class).listarNotificacionesPedido()).withRel("notificaciones"));
    }
}
