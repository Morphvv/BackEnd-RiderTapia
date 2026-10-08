package cl.ridertapia.reportesservice.service;

import cl.ridertapia.reportesservice.client.DetallePedidoDto;
import cl.ridertapia.reportesservice.client.PedidoDto;
import cl.ridertapia.reportesservice.client.PedidosClient;
import cl.ridertapia.reportesservice.dto.ReporteDto;
import cl.ridertapia.reportesservice.exception.ApiException;
import cl.ridertapia.reportesservice.model.ProductoMasVendido;
import cl.ridertapia.reportesservice.model.ReporteVentas;
import cl.ridertapia.reportesservice.repository.ProductoMasVendidoRepository;
import cl.ridertapia.reportesservice.repository.ReporteVentasRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteVentasService {

    private static final int TOP_MAXIMO = 50;

    private final ReporteVentasRepository reporteRepository;
    private final ProductoMasVendidoRepository masVendidoRepository;
    private final PedidosClient pedidosClient;
    private final CriterioVenta criterioVenta;
    private final int topPorDefecto;

    public ReporteVentasService(ReporteVentasRepository reporteRepository,
                                ProductoMasVendidoRepository masVendidoRepository,
                                PedidosClient pedidosClient,
                                CriterioVenta criterioVenta,
                                @Value("${reportes.top-productos:10}") int topPorDefecto) {
        this.reporteRepository = reporteRepository;
        this.masVendidoRepository = masVendidoRepository;
        this.pedidosClient = pedidosClient;
        this.criterioVenta = criterioVenta;
        this.topPorDefecto = topPorDefecto;
    }

    /** Ventas de un solo día. Sin fecha, usa hoy. */
    @Transactional
    public ReporteDto.Detalle generarReporteDiario(LocalDate fecha, Integer top, String generadoPor) {
        LocalDate dia = fecha == null ? LocalDate.now() : fecha;
        return generar(ReporteVentas.Tipo.DIARIO, dia, dia, top, generadoPor);
    }

    /** Ventas de la semana (lunes a domingo) que contiene la fecha. Sin fecha, usa hoy. */
    @Transactional
    public ReporteDto.Detalle generarReporteSemanal(LocalDate fecha, Integer top, String generadoPor) {
        LocalDate base = fecha == null ? LocalDate.now() : fecha;
        LocalDate lunes = base.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return generar(ReporteVentas.Tipo.SEMANAL, lunes, lunes.plusDays(6), top, generadoPor);
    }

    /** Ventas del mes completo que contiene la fecha. Sin fecha, usa hoy. */
    @Transactional
    public ReporteDto.Detalle generarReporteMensual(LocalDate fecha, Integer top, String generadoPor) {
        LocalDate base = fecha == null ? LocalDate.now() : fecha;
        return generar(ReporteVentas.Tipo.MENSUAL,
                base.withDayOfMonth(1), base.with(TemporalAdjusters.lastDayOfMonth()), top, generadoPor);
    }

    @Transactional(readOnly = true)
    public List<ReporteDto.Resumen> listarReportes(ReporteVentas.Tipo tipo) {
        List<ReporteVentas> reportes = tipo == null
                ? reporteRepository.findAllByOrderByFechaInicioDesc()
                : reporteRepository.findByTipoOrderByFechaInicioDesc(tipo);
        return reportes.stream().map(ReporteDto.Resumen::desde).toList();
    }

    @Transactional(readOnly = true)
    public ReporteDto.Detalle findById(Long idReporte) {
        ReporteVentas reporte = reporteRepository.findById(idReporte)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el reporte " + idReporte));
        return ReporteDto.Detalle.desde(reporte,
                masVendidoRepository.findByIdReporteOrderByCantidadVendidaDescTotalVendidoDesc(idReporte));
    }

    @Transactional(readOnly = true)
    public List<ReporteDto.MasVendido> listarMasVendidos(Long idReporte) {
        return findById(idReporte).masVendidos();
    }

    private ReporteDto.Detalle generar(ReporteVentas.Tipo tipo, LocalDate inicio, LocalDate fin,
                                       Integer top, String generadoPor) {
        int limite = top == null ? topPorDefecto : top;
        if (limite < 1 || limite > TOP_MAXIMO) {
            throw ApiException.solicitudInvalida("El parámetro 'top' debe estar entre 1 y " + TOP_MAXIMO);
        }

        // Pedidos del período que cuentan como venta
        List<PedidoDto> pedidos = pedidosClient.listarPedidos().stream()
                .filter(p -> p.fechaCreacion() != null)
                .filter(p -> {
                    LocalDate dia = p.fechaCreacion().toLocalDate();
                    return !dia.isBefore(inicio) && !dia.isAfter(fin);
                })
                .filter(p -> criterioVenta.esVenta(p.estado()))
                .toList();

        long totalVentas = pedidos.stream().mapToLong(p -> valor(p.total())).sum();

        // Por variante: [0] = unidades vendidas, [1] = monto vendido
        Map<Long, long[]> acumulado = new HashMap<>();
        if (!pedidos.isEmpty()) {
            Set<Long> idsPedidos = pedidos.stream().map(PedidoDto::idPedido).collect(Collectors.toSet());
            for (DetallePedidoDto detalle : pedidosClient.listarDetalles()) {
                if (detalle.pedido() == null || detalle.idVariante() == null
                        || !idsPedidos.contains(detalle.pedido().idPedido())) {
                    continue;
                }
                long[] acc = acumulado.computeIfAbsent(detalle.idVariante(), k -> new long[2]);
                acc[0] += valor(detalle.cantidad());
                acc[1] += valor(detalle.subtotal());
            }
        }

        // Si ya existe un reporte del mismo tipo y período, se recalcula en vez de duplicarlo
        ReporteVentas reporte = reporteRepository.findByTipoAndFechaInicioAndFechaFin(tipo, inicio, fin)
                .orElseGet(() -> ReporteVentas.builder().tipo(tipo).fechaInicio(inicio).fechaFin(fin).build());
        reporte.setTotalVentas(totalVentas);
        reporte.setCantidadPedidos(pedidos.size());
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setGeneradoPor(generadoPor == null || generadoPor.isBlank() ? null : generadoPor.trim());
        final ReporteVentas guardado = reporteRepository.save(reporte);

        masVendidoRepository.deleteByIdReporte(guardado.getIdReporte());
        List<ProductoMasVendido> ranking = acumulado.entrySet().stream()
                .sorted((a, b) -> {
                    int porUnidades = Long.compare(b.getValue()[0], a.getValue()[0]);
                    return porUnidades != 0 ? porUnidades : Long.compare(b.getValue()[1], a.getValue()[1]);
                })
                .limit(limite)
                .map(e -> ProductoMasVendido.builder()
                        .idReporte(guardado.getIdReporte())
                        .idVariante(e.getKey())
                        .cantidadVendida((int) e.getValue()[0])
                        .totalVendido(e.getValue()[1])
                        .build())
                .toList();
        List<ProductoMasVendido> rankingGuardado = masVendidoRepository.saveAll(ranking);

        return ReporteDto.Detalle.desde(guardado, rankingGuardado);
    }

    private long valor(Integer numero) {
        return numero == null ? 0L : numero;
    }
}
