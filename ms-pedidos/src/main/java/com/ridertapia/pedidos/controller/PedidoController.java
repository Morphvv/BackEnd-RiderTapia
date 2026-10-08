package com.ridertapia.pedidos.controller;

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

import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.service.PedidoService;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> listarPedidos() {
        CollectionModel<EntityModel<Pedido>> coleccion = CollectionModel.of(
                toModelList(pedidoService.listarPedidos()),
                linkTo(methodOn(PedidoController.class).listarPedidos()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> obtenerPorId(@PathVariable Long id) {
        return pedidoService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<EntityModel<Pedido>> obtenerPorNumero(@PathVariable String numero) {
        Pedido pedido = pedidoService.findByNumero(numero);
        if (pedido == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toModel(pedido));
    }

    @PostMapping
    public ResponseEntity<EntityModel<Pedido>> crearPedido(@RequestBody Pedido pedido) {
        try {
            Pedido nuevo = pedidoService.guardarPedido(pedido);
            return ResponseEntity
                    .created(linkTo(methodOn(PedidoController.class).obtenerPorId(nuevo.getIdPedido())).toUri())
                    .body(toModel(nuevo));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Pedido>> actualizarPedido(@PathVariable Long id, @RequestBody Pedido pedido) {
        try {
            Pedido actualizado = pedidoService.actualizarPedido(id, pedido);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPedido(@PathVariable Long id) {
        try {
            pedidoService.eliminarPedido(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private List<EntityModel<Pedido>> toModelList(List<Pedido> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(PedidoController.class).obtenerPorId(x.getIdPedido())).withSelfRel()))
                .toList();
    }

    private EntityModel<Pedido> toModel(Pedido pedido) {
        return EntityModel.of(pedido,
                linkTo(methodOn(PedidoController.class).obtenerPorId(pedido.getIdPedido())).withSelfRel(),
                linkTo(methodOn(PedidoController.class).listarPedidos()).withRel("pedidos"));
    }
}
