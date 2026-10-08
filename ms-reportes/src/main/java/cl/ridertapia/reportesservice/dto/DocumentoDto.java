package cl.ridertapia.reportesservice.dto;

import cl.ridertapia.reportesservice.model.DatosFactura;
import cl.ridertapia.reportesservice.model.DetalleDocumento;
import cl.ridertapia.reportesservice.model.DocumentoVenta;
import cl.ridertapia.reportesservice.util.RutUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.hateoas.server.core.Relation;

public final class DocumentoDto {

    private DocumentoDto() {
    }

    @Schema(description = "Datos para emitir una boleta")
    public record EmitirBoleta(
            @NotNull(message = "El id del pedido es obligatorio")
            @Schema(example = "1")
            Long idPedido
    ) {
    }

    @Schema(description = "Datos para emitir una factura")
    public record EmitirFactura(
            @NotNull(message = "El id del pedido es obligatorio")
            @Schema(example = "1")
            Long idPedido,

            @NotBlank(message = "El RUT de la empresa es obligatorio")
            @Pattern(regexp = RutUtil.RUT_REGEX, message = "El RUT de la empresa debe tener formato 76123456-0")
            @Schema(example = "76543210-3")
            String rutEmpresa,

            @NotBlank(message = "La razón social es obligatoria")
            @Size(max = 150, message = "La razón social no puede superar los 150 caracteres")
            @Schema(example = "Ciclos del Sur SpA")
            String razonSocial,

            @NotBlank(message = "El giro es obligatorio")
            @Size(max = 150, message = "El giro no puede superar los 150 caracteres")
            @Schema(example = "Venta de bicicletas y accesorios")
            String giro,

            @NotBlank(message = "La dirección de la empresa es obligatoria")
            @Size(max = 200, message = "La dirección no puede superar los 200 caracteres")
            @Schema(example = "Av. Colón 1234, Talcahuano")
            String direccionEmpresa
    ) {
    }

    /** Datos del documento sin sus líneas (para listados). */
    @Relation(collectionRelation = "documentos", itemRelation = "documento")
    @Schema(description = "Encabezado de una boleta o factura")
    public record Resumen(
            Long idDocumento,
            Long numero,
            DocumentoVenta.Tipo tipo,
            Long idPedido,
            String rutCliente,
            LocalDateTime fechaEmision,
            Integer neto,
            Integer iva,
            Integer total
    ) {
        public static Resumen desde(DocumentoVenta d) {
            return new Resumen(d.getIdDocumento(), d.getNumero(), d.getTipo(), d.getIdPedido(),
                    d.getRutCliente(), d.getFechaEmision(), d.getNeto(), d.getIva(), d.getTotal());
        }
    }

    public record Linea(Long idVariante, String descripcion, Integer cantidad,
                        Integer precioUnitario, Integer subtotal) {
        public static Linea desde(DetalleDocumento d) {
            return new Linea(d.getIdVariante(), d.getDescripcion(), d.getCantidad(),
                    d.getPrecioUnitario(), d.getSubtotal());
        }
    }

    public record DatosEmpresa(String rutEmpresa, String razonSocial, String giro, String direccionEmpresa) {
        public static DatosEmpresa desde(DatosFactura f) {
            return new DatosEmpresa(f.getRutEmpresa(), f.getRazonSocial(), f.getGiro(), f.getDireccionEmpresa());
        }
    }

    /** Documento completo: encabezado, líneas y, si es factura, los datos de la empresa. */
    @Schema(description = "Documento completo: encabezado, líneas y datos de la empresa (solo facturas)")
    public record Detalle(Resumen documento, List<Linea> lineas, DatosEmpresa datosFactura) {
    }
}
