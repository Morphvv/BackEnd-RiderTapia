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

import com.ridertapia.productos.model.VarianteProducto;
import com.ridertapia.productos.service.VarianteProductoService;

@RestController
@RequestMapping("/api/v1/variantes")
public class VarianteProductoController {

    @Autowired
    private VarianteProductoService varianteProductoService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<VarianteProducto>>> listarVariantesProducto() {
        CollectionModel<EntityModel<VarianteProducto>> coleccion = CollectionModel.of(
                toModelList(varianteProductoService.listarVariantesProducto()),
                linkTo(methodOn(VarianteProductoController.class).listarVariantesProducto()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<VarianteProducto>> obtenerPorId(@PathVariable Long id) {
        return varianteProductoService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<EntityModel<VarianteProducto>> obtenerPorSku(@PathVariable String sku) {
        VarianteProducto varianteProducto = varianteProductoService.findBySku(sku);
        if (varianteProducto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toModel(varianteProducto));
    }

    @PostMapping
    public ResponseEntity<EntityModel<VarianteProducto>> crearVarianteProducto(@RequestBody VarianteProducto varianteProducto) {
        try {
            VarianteProducto nuevo = varianteProductoService.guardarVarianteProducto(varianteProducto);
            return ResponseEntity
                    .created(linkTo(methodOn(VarianteProductoController.class).obtenerPorId(nuevo.getIdVarianteProducto())).toUri())
                    .body(toModel(nuevo));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<VarianteProducto>> actualizarVarianteProducto(@PathVariable Long id, @RequestBody VarianteProducto varianteProducto) {
        try {
            VarianteProducto actualizado = varianteProductoService.actualizarVarianteProducto(id, varianteProducto);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (DataIntegrityViolationException e) {
            // Valor único repetido
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVarianteProducto(@PathVariable Long id) {
        varianteProductoService.eliminarVarianteProducto(id);
        return ResponseEntity.noContent().build();
    }

    private List<EntityModel<VarianteProducto>> toModelList(List<VarianteProducto> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(VarianteProductoController.class).obtenerPorId(x.getIdVarianteProducto())).withSelfRel()))
                .toList();
    }

    private EntityModel<VarianteProducto> toModel(VarianteProducto varianteProducto) {
        return EntityModel.of(varianteProducto,
                linkTo(methodOn(VarianteProductoController.class).obtenerPorId(varianteProducto.getIdVarianteProducto())).withSelfRel(),
                linkTo(methodOn(VarianteProductoController.class).listarVariantesProducto()).withRel("variantes"));
    }
}
