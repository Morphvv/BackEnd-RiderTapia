package com.ridertapia.pagos.controller;

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

import com.ridertapia.pagos.model.PagoWebPay;
import com.ridertapia.pagos.service.PagoWebPayService;

@RestController
@RequestMapping("/api/v1/pagos-webpay")
public class PagoWebPayController {

    @Autowired
    private PagoWebPayService pagoWebPayService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PagoWebPay>>> listarPagosWebPay() {
        CollectionModel<EntityModel<PagoWebPay>> coleccion = CollectionModel.of(
                toModelList(pagoWebPayService.listarPagosWebPay()),
                linkTo(methodOn(PagoWebPayController.class).listarPagosWebPay()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<PagoWebPay>> obtenerPorId(@PathVariable Long id) {
        return pagoWebPayService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/idPedido/{idPedido}")
    public ResponseEntity<CollectionModel<EntityModel<PagoWebPay>>> obtenerPorIdPedido(@PathVariable Long idPedido) {
        CollectionModel<EntityModel<PagoWebPay>> coleccion = CollectionModel.of(
                toModelList(pagoWebPayService.findByIdPedido(idPedido)),
                linkTo(methodOn(PagoWebPayController.class).obtenerPorIdPedido(idPedido)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<PagoWebPay>> crearPagoWebPay(@RequestBody PagoWebPay pagoWebPay) {
        try {
            PagoWebPay nuevo = pagoWebPayService.guardarPagoWebPay(pagoWebPay);
            return ResponseEntity
                    .created(linkTo(methodOn(PagoWebPayController.class).obtenerPorId(nuevo.getIdPago())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<PagoWebPay>> actualizarPagoWebPay(@PathVariable Long id, @RequestBody PagoWebPay pagoWebPay) {
        try {
            PagoWebPay actualizado = pagoWebPayService.actualizarPagoWebPay(id, pagoWebPay);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPagoWebPay(@PathVariable Long id) {
        pagoWebPayService.eliminarPagoWebPay(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<PagoWebPay>> toModelList(List<PagoWebPay> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(PagoWebPayController.class).obtenerPorId(x.getIdPago())).withSelfRel()))
                .toList();
    }

    private EntityModel<PagoWebPay> toModel(PagoWebPay pagoWebPay) {
        return EntityModel.of(pagoWebPay,
                linkTo(methodOn(PagoWebPayController.class).obtenerPorId(pagoWebPay.getIdPago())).withSelfRel(),
                linkTo(methodOn(PagoWebPayController.class).listarPagosWebPay()).withRel("pagosWebpay"));
    }
}
