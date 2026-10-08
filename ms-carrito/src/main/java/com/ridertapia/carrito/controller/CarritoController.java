package com.ridertapia.carrito.controller;

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

import com.ridertapia.carrito.dto.AvisoCambio;
import com.ridertapia.carrito.dto.ResultadoValidacionCupon;
import com.ridertapia.carrito.dto.ResumenCarrito;
import com.ridertapia.carrito.model.Carrito;
import com.ridertapia.carrito.service.CarritoService;

@RestController
@RequestMapping("/api/v1/carritos")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Carrito>>> listarCarritos() {
        CollectionModel<EntityModel<Carrito>> coleccion = CollectionModel.of(
                toModelList(carritoService.listarCarritos()),
                linkTo(methodOn(CarritoController.class).listarCarritos()).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Carrito>> obtenerPorId(@PathVariable Long id) {
        return carritoService.findById(id)
                .map(x -> ResponseEntity.ok(toModel(x)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/rutCliente/{rutCliente}")
    public ResponseEntity<CollectionModel<EntityModel<Carrito>>> obtenerPorRutCliente(@PathVariable String rutCliente) {
        CollectionModel<EntityModel<Carrito>> coleccion = CollectionModel.of(
                toModelList(carritoService.findByRutCliente(rutCliente)),
                linkTo(methodOn(CarritoController.class).obtenerPorRutCliente(rutCliente)).withSelfRel());
        return ResponseEntity.ok(coleccion);
    }

    @PostMapping
    public ResponseEntity<EntityModel<Carrito>> crearCarrito(@RequestBody Carrito carrito) {
        try {
            Carrito nuevo = carritoService.guardarCarrito(carrito);
            return ResponseEntity
                    .created(linkTo(methodOn(CarritoController.class).obtenerPorId(nuevo.getIdCarrito())).toUri())
                    .body(toModel(nuevo));
        } catch (RuntimeException e) {
            // El RUT no existe en ms-cliente
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Carrito>> actualizarCarrito(@PathVariable Long id, @RequestBody Carrito carrito) {
        try {
            Carrito actualizado = carritoService.actualizarCarrito(id, carrito);
            return ResponseEntity.ok(toModel(actualizado));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCarrito(@PathVariable Long id) {
        try {
            carritoService.eliminarCarrito(id);
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException e) {
            // Está siendo usado por otro registro
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/{id}/resumen")
    public ResponseEntity<EntityModel<ResumenCarrito>> obtenerResumen(@PathVariable Long id) {
        try {
            ResumenCarrito resumen = carritoService.obtenerResumen(id);
            return ResponseEntity.ok(EntityModel.of(resumen,
                    linkTo(methodOn(CarritoController.class).obtenerResumen(id)).withSelfRel(),
                    linkTo(methodOn(CarritoController.class).obtenerPorId(id)).withRel("carrito")));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/avisos")
    public ResponseEntity<CollectionModel<AvisoCambio>> revalidar(@PathVariable Long id) {
        try {
            List<AvisoCambio> avisos = carritoService.revalidar(id);
            return ResponseEntity.ok(CollectionModel.of(avisos,
                    linkTo(methodOn(CarritoController.class).revalidar(id)).withSelfRel(),
                    linkTo(methodOn(CarritoController.class).obtenerPorId(id)).withRel("carrito")));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/cupon/{codigo}")
    public ResponseEntity<EntityModel<ResultadoValidacionCupon>> validarCupon(@PathVariable Long id,
            @PathVariable String codigo) {
        try {
            ResultadoValidacionCupon resultado = carritoService.validarCupon(id, codigo);
            return ResponseEntity.ok(EntityModel.of(resultado,
                    linkTo(methodOn(CarritoController.class).validarCupon(id, codigo)).withSelfRel(),
                    linkTo(methodOn(CarritoController.class).obtenerPorId(id)).withRel("carrito")));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private List<EntityModel<Carrito>> toModelList(List<Carrito> lista) {
        return lista.stream()
                .map(x -> EntityModel.of(x,
                        linkTo(methodOn(CarritoController.class).obtenerPorId(x.getIdCarrito())).withSelfRel()))
                .toList();
    }

    private EntityModel<Carrito> toModel(Carrito carrito) {
        return EntityModel.of(carrito,
                linkTo(methodOn(CarritoController.class).obtenerPorId(carrito.getIdCarrito())).withSelfRel(),
                linkTo(methodOn(CarritoController.class).listarCarritos()).withRel("carritos"));
    }
}
