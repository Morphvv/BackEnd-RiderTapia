package cl.ridertapia.reportesservice.dto;

import cl.ridertapia.reportesservice.model.ProductoMasVendido;
import cl.ridertapia.reportesservice.model.ReporteVentas;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.hateoas.server.core.Relation;

public final class ReporteDto {

    private ReporteDto() {
    }

    /** Datos del reporte sin el ranking (para listados). */
    @Relation(collectionRelation = "reportes", itemRelation = "reporte")
    @Schema(description = "Resumen de un reporte de ventas")
    public record Resumen(
            Long idReporte,
            ReporteVentas.Tipo tipo,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            Long totalVentas,
            Integer cantidadPedidos,
            LocalDateTime fechaGeneracion,
            String generadoPor
    ) {
        public static Resumen desde(ReporteVentas r) {
            return new Resumen(r.getIdReporte(), r.getTipo(), r.getFechaInicio(), r.getFechaFin(),
                    r.getTotalVentas(), r.getCantidadPedidos(), r.getFechaGeneracion(), r.getGeneradoPor());
        }
    }

    @Relation(collectionRelation = "masVendidos", itemRelation = "masVendido")
    @Schema(description = "Una fila del ranking de productos más vendidos")
    public record MasVendido(
            int posicion,
            Long idVariante,
            String nombreProducto,
            Integer cantidadVendida,
            Long totalVendido
    ) {
    }

    /** Reporte completo con su ranking de productos más vendidos. */
    @Schema(description = "Reporte con su ranking de más vendidos")
    public record Detalle(Resumen reporte, List<MasVendido> masVendidos) {

        public static Detalle desde(ReporteVentas reporte, List<ProductoMasVendido> ranking) {
            List<MasVendido> filas = IntStream.range(0, ranking.size())
                    .mapToObj(i -> {
                        ProductoMasVendido p = ranking.get(i);
                        return new MasVendido(i + 1, p.getIdVariante(), p.getNombreProducto(),
                                p.getCantidadVendida(), p.getTotalVendido());
                    })
                    .toList();
            return new Detalle(Resumen.desde(reporte), filas);
        }
    }
}
