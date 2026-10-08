package com.ridertapia.productos.controller;

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

import com.ridertapia.productos.model.Cupon;
import com.ridertapia.productos.service.CuponService;

@RestController
@RequestMapping("/api/v1/cupones")
public class CuponController {

    @Autowired
    private CuponService cuponService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Cupon>>> listarCupones() {
        CollectionModel<EntityModel<Cupon>> coleccion = CollectionModel.of(
                toModelList(cuponService.listarCupones()),
                linkTo(methodOn(CuponController.class).listarCupones()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Cupon>> obtenerPorId(@PathVariable Long id) {
        return cuponService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<EntityModel<Cupon>> obtenerPorCodigo(@PathVariable String codigo) {
        Cupon cupon = cuponService.findByCodigo(codigo);
        if (cupon == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toModel(cupon));
    }

    @PostMapping
    public ResponseEntity<EntityModel<Cupon>> crearCupon(@RequestBody Cupon cupon) {
        try {
            Cupon nuevo = cuponService.guardarCupon(cupon);
            return ResponseEntity
                    .created(linkTo(methodOn(CuponController.class).obtenerPorId(nuevo.getIdCupon())).toUri())
                    .body(toModel(nuevo));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Cupon>> actualizarCupon(@PathVariable Long id, @RequestBody Cupon cupon) {
        try {
            Cupon actualizado = cuponService.actualizarCupon(id, cupon);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCupon(@PathVariable Long id) {
        cuponService.eliminarCupon(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<Cupon>> toModelList(List<Cupon> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(CuponController.class).obtenerPorId(x.getIdCupon())).withSelfRel()))
                .toList();
    }

    private EntityModel<Cupon> toModel(Cupon cupon) {
        return EntityModel.of(cupon,
                linkTo(methodOn(CuponController.class).obtenerPorId(cupon.getIdCupon())).withSelfRel(),
                linkTo(methodOn(CuponController.class).listarCupones()).withRel("cupones"));
    }
}
