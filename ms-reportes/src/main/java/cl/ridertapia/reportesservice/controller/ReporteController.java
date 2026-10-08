package cl.ridertapia.reportesservice.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import cl.ridertapia.reportesservice.dto.ReporteDto;
import cl.ridertapia.reportesservice.model.ReporteVentas;
import cl.ridertapia.reportesservice.service.ReporteVentasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes de ventas", description = "Reportes diarios, semanales y mensuales con los productos más vendidos")
public class ReporteController {

    private final ReporteVentasService reporteService;
    private final ReporteModelAssembler assembler;

    // "fecha" va en formato 2026-10-07; si se omite se usa hoy. El período se calcula a partir de esa fecha.

    @Operation(summary = "Generar el reporte diario",
            description = "Suma las ventas del día indicado (hoy si no se envía fecha). "
                    + "Si ya existía un reporte de ese día, se recalcula.")
    @ApiResponse(responseCode = "201", description = "Reporte generado")
    @ApiResponse(responseCode = "400", description = "Fecha mal escrita o 'top' fuera de rango (1 a 50)")
    @ApiResponse(responseCode = "503", description = "El servicio de pedidos no está disponible")
    @PostMapping("/diario")
    public ResponseEntity<EntityModel<ReporteDto.Detalle>> generarReporteDiario(
            @Parameter(description = "Día del reporte (AAAA-MM-DD)", example = "2026-10-07")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(description = "Cuántos productos incluye el ranking (1 a 50)", example = "10")
            @RequestParam(required = false) Integer top,
            @Parameter(description = "Quién genera el reporte", example = "pedro")
            @RequestParam(required = false) String generadoPor) {
        return creado(reporteService.generarReporteDiario(fecha, top, generadoPor));
    }

    @Operation(summary = "Generar el reporte semanal",
            description = "Suma las ventas de la semana (lunes a domingo) que contiene la fecha indicada.")
    @ApiResponse(responseCode = "201", description = "Reporte generado")
    @ApiResponse(responseCode = "400", description = "Fecha mal escrita o 'top' fuera de rango (1 a 50)")
    @ApiResponse(responseCode = "503", description = "El servicio de pedidos no está disponible")
    @PostMapping("/semanal")
    public ResponseEntity<EntityModel<ReporteDto.Detalle>> generarReporteSemanal(
            @Parameter(description = "Cualquier día de la semana (AAAA-MM-DD)", example = "2026-10-07")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer top,
            @RequestParam(required = false) String generadoPor) {
        return creado(reporteService.generarReporteSemanal(fecha, top, generadoPor));
    }

    @Operation(summary = "Generar el reporte mensual",
            description = "Suma las ventas del mes completo que contiene la fecha indicada.")
    @ApiResponse(responseCode = "201", description = "Reporte generado")
    @ApiResponse(responseCode = "400", description = "Fecha mal escrita o 'top' fuera de rango (1 a 50)")
    @ApiResponse(responseCode = "503", description = "El servicio de pedidos no está disponible")
    @PostMapping("/mensual")
    public ResponseEntity<EntityModel<ReporteDto.Detalle>> generarReporteMensual(
            @Parameter(description = "Cualquier día del mes (AAAA-MM-DD)", example = "2026-10-07")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer top,
            @RequestParam(required = false) String generadoPor) {
        return creado(reporteService.generarReporteMensual(fecha, top, generadoPor));
    }

    @Operation(summary = "Listar los reportes generados", description = "Del más reciente al más antiguo.")
    @ApiResponse(responseCode = "200", description = "Lista de reportes")
    @GetMapping
    public CollectionModel<EntityModel<ReporteDto.Resumen>> listarReportes(
            @Parameter(description = "Filtrar por tipo: DIARIO, SEMANAL o MENSUAL")
            @RequestParam(required = false) ReporteVentas.Tipo tipo) {
        List<EntityModel<ReporteDto.Resumen>> reportes = reporteService.listarReportes(tipo).stream()
                .map(assembler::resumen)
                .toList();
        return CollectionModel.of(reportes,
                linkTo(methodOn(ReporteController.class).listarReportes(tipo)).withSelfRel());
    }

    @Operation(summary = "Obtener un reporte con su ranking de más vendidos")
    @ApiResponse(responseCode = "200", description = "Reporte encontrado")
    @ApiResponse(responseCode = "404", description = "No existe el reporte")
    @GetMapping("/{id}")
    public EntityModel<ReporteDto.Detalle> findById(@PathVariable Long id) {
        return assembler.detalle(reporteService.findById(id));
    }

    @Operation(summary = "Obtener solo el ranking de productos más vendidos de un reporte",
            description = "Ordenado por unidades vendidas y, en empate, por monto vendido.")
    @ApiResponse(responseCode = "200", description = "Ranking (puede estar vacío si no hubo ventas)")
    @ApiResponse(responseCode = "404", description = "No existe el reporte")
    @GetMapping("/{id}/mas-vendidos")
    public CollectionModel<ReporteDto.MasVendido> listarMasVendidos(@PathVariable Long id) {
        return CollectionModel.of(reporteService.listarMasVendidos(id),
                linkTo(methodOn(ReporteController.class).listarMasVendidos(id)).withSelfRel(),
                linkTo(methodOn(ReporteController.class).findById(id)).withRel("reporte"));
    }

    private ResponseEntity<EntityModel<ReporteDto.Detalle>> creado(ReporteDto.Detalle detalle) {
        EntityModel<ReporteDto.Detalle> modelo = assembler.detalle(detalle);
        return ResponseEntity.created(modelo.getRequiredLink("self").toUri()).body(modelo);
    }
}
