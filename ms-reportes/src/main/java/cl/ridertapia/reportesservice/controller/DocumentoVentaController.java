package cl.ridertapia.reportesservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.reportesservice.dto.DocumentoDto;
import cl.ridertapia.reportesservice.model.DocumentoVenta;
import cl.ridertapia.reportesservice.service.DocumentoVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documentos")
@RequiredArgsConstructor
@Tag(name = "Boletas y facturas", description = "Emisión y consulta de documentos de venta")
public class DocumentoVentaController {

    private final DocumentoVentaService documentoService;
    private final DocumentoModelAssembler assembler;

    @Operation(summary = "Emitir una boleta",
            description = "Solo para pedidos en estado de venta. Un pedido admite un único documento. "
                    + "El total ya incluye IVA (19%).")
    @ApiResponse(responseCode = "201", description = "Boleta emitida")
    @ApiResponse(responseCode = "400", description = "Pedido sin pagar, sin productos o sin total válido")
    @ApiResponse(responseCode = "404", description = "No existe el pedido")
    @ApiResponse(responseCode = "409", description = "El pedido ya tiene un documento emitido")
    @ApiResponse(responseCode = "503", description = "El servicio de pedidos no está disponible")
    @PostMapping("/boleta")
    public ResponseEntity<EntityModel<DocumentoDto.Detalle>> emitirBoleta(
            @Valid @RequestBody DocumentoDto.EmitirBoleta request) {
        return creado(documentoService.emitirBoleta(request));
    }

    @Operation(summary = "Emitir una factura",
            description = "Igual que la boleta, pero además lleva los datos de la empresa (RUT con dígito verificador válido).")
    @ApiResponse(responseCode = "201", description = "Factura emitida")
    @ApiResponse(responseCode = "400", description = "Datos inválidos, RUT de empresa incorrecto o pedido sin pagar")
    @ApiResponse(responseCode = "404", description = "No existe el pedido")
    @ApiResponse(responseCode = "409", description = "El pedido ya tiene un documento emitido")
    @ApiResponse(responseCode = "503", description = "El servicio de pedidos no está disponible")
    @PostMapping("/factura")
    public ResponseEntity<EntityModel<DocumentoDto.Detalle>> emitirFactura(
            @Valid @RequestBody DocumentoDto.EmitirFactura request) {
        return creado(documentoService.emitirFactura(request));
    }

    @Operation(summary = "Listar documentos emitidos")
    @ApiResponse(responseCode = "200", description = "Lista de documentos")
    @GetMapping
    public CollectionModel<EntityModel<DocumentoDto.Resumen>> listarDocumentos(
            @Parameter(description = "Filtrar por tipo: BOLETA o FACTURA")
            @RequestParam(required = false) DocumentoVenta.Tipo tipo) {
        List<EntityModel<DocumentoDto.Resumen>> documentos = documentoService.listarDocumentos(tipo).stream()
                .map(assembler::resumen)
                .toList();
        return CollectionModel.of(documentos,
                linkTo(methodOn(DocumentoVentaController.class).listarDocumentos(tipo)).withSelfRel());
    }

    @Operation(summary = "Obtener un documento por su id")
    @ApiResponse(responseCode = "200", description = "Documento encontrado")
    @ApiResponse(responseCode = "404", description = "No existe el documento")
    @GetMapping("/{id}")
    public EntityModel<DocumentoDto.Detalle> findById(@PathVariable Long id) {
        return assembler.detalle(documentoService.findById(id));
    }

    @Operation(summary = "Obtener el documento emitido para un pedido")
    @ApiResponse(responseCode = "200", description = "Documento encontrado")
    @ApiResponse(responseCode = "404", description = "El pedido no tiene documento emitido")
    @GetMapping("/pedido/{idPedido}")
    public EntityModel<DocumentoDto.Detalle> findByPedido(@PathVariable Long idPedido) {
        return assembler.detalle(documentoService.findByPedido(idPedido));
    }

    @Operation(summary = "Buscar un documento por tipo y número",
            description = "Cada tipo tiene su propia numeración correlativa (boleta 1, 2, 3... y factura 1, 2, 3...).")
    @ApiResponse(responseCode = "200", description = "Documento encontrado")
    @ApiResponse(responseCode = "404", description = "No existe ese documento")
    @GetMapping("/buscar")
    public EntityModel<DocumentoDto.Detalle> findByNumero(
            @Parameter(description = "BOLETA o FACTURA") @RequestParam DocumentoVenta.Tipo tipo,
            @Parameter(description = "Número correlativo", example = "1") @RequestParam Long numero) {
        return assembler.detalle(documentoService.findByNumero(tipo, numero));
    }

    @Operation(summary = "Listar los documentos de un cliente",
            description = "El RUT del cliente, con guion (ej: 12345678-5).")
    @ApiResponse(responseCode = "200", description = "Documentos del cliente (puede ser una lista vacía)")
    @GetMapping("/cliente/{rut}")
    public CollectionModel<EntityModel<DocumentoDto.Resumen>> findByRutCliente(@PathVariable String rut) {
        List<EntityModel<DocumentoDto.Resumen>> documentos = documentoService.findByRutCliente(rut).stream()
                .map(assembler::resumen)
                .toList();
        return CollectionModel.of(documentos,
                linkTo(methodOn(DocumentoVentaController.class).findByRutCliente(rut)).withSelfRel());
    }

    private ResponseEntity<EntityModel<DocumentoDto.Detalle>> creado(DocumentoDto.Detalle detalle) {
        EntityModel<DocumentoDto.Detalle> modelo = assembler.detalle(detalle);
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }
}
