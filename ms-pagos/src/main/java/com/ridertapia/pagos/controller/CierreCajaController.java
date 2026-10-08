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

import com.ridertapia.pagos.model.CierreCaja;
import com.ridertapia.pagos.service.CierreCajaService;

@RestController
@RequestMapping("/api/v1/cierres-caja")
public class CierreCajaController {

    @Autowired
    private CierreCajaService cierreCajaService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<CierreCaja>>> listarCierresCaja() {
        CollectionModel<EntityModel<CierreCaja>> coleccion = CollectionModel.of(
                toModelList(cierreCajaService.listarCierresCaja()),
                linkTo(methodOn(CierreCajaController.class).listarCierresCaja()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<CierreCaja>> obtenerPorId(@PathVariable Long id) {
        return cierreCajaService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/idEmpleado/{idEmpleado}")
    public ResponseEntity<CollectionModel<EntityModel<CierreCaja>>> obtenerPorIdEmpleado(@PathVariable Long idEmpleado) {
        CollectionModel<EntityModel<CierreCaja>> coleccion = CollectionModel.of(
                toModelList(cierreCajaService.findByIdEmpleado(idEmpleado)),
                linkTo(methodOn(CierreCajaController.class).obtenerPorIdEmpleado(idEmpleado)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<CierreCaja>> crearCierreCaja(@RequestBody CierreCaja cierreCaja) {
        CierreCaja nuevo = cierreCajaService.guardarCierreCaja(cierreCaja);
        return ResponseEntity
                .created(linkTo(methodOn(CierreCajaController.class).obtenerPorId(nuevo.getIdCierreCaja())).toUri())
                .body(toModel(nuevo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<CierreCaja>> actualizarCierreCaja(@PathVariable Long id, @RequestBody CierreCaja cierreCaja) {
        try {
            CierreCaja actualizado = cierreCajaService.actualizarCierreCaja(id, cierreCaja);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCierreCaja(@PathVariable Long id) {
        cierreCajaService.eliminarCierreCaja(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<CierreCaja>> toModelList(List<CierreCaja> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(CierreCajaController.class).obtenerPorId(x.getIdCierreCaja())).withSelfRel()))
                .toList();
    }

    private EntityModel<CierreCaja> toModel(CierreCaja cierreCaja) {
        return EntityModel.of(cierreCaja,
                linkTo(methodOn(CierreCajaController.class).obtenerPorId(cierreCaja.getIdCierreCaja())).withSelfRel(),
                linkTo(methodOn(CierreCajaController.class).listarCierresCaja()).withRel("cierresCaja"));
    }
}
