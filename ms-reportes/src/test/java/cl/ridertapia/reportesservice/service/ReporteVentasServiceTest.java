package cl.ridertapia.reportesservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.ridertapia.reportesservice.client.DetallePedidoDto;
import cl.ridertapia.reportesservice.client.PedidoDto;
import cl.ridertapia.reportesservice.client.PedidosClient;
import cl.ridertapia.reportesservice.dto.ReporteDto;
import cl.ridertapia.reportesservice.exception.ApiException;
import cl.ridertapia.reportesservice.model.ProductoMasVendido;
import cl.ridertapia.reportesservice.model.ReporteVentas;
import cl.ridertapia.reportesservice.repository.ProductoMasVendidoRepository;
import cl.ridertapia.reportesservice.repository.ReporteVentasRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class ReporteVentasServiceTest {

    // Miércoles 7 de octubre de 2026
    private static final LocalDate HOY = LocalDate.of(2026, 10, 7);

    @Mock
    private ReporteVentasRepository reporteRepository;

    @Mock
    private ProductoMasVendidoRepository masVendidoRepository;

    @Mock
    private PedidosClient pedidosClient;

    private ReporteVentasService service;

    @BeforeEach
    void preparar() {
        service = new ReporteVentasService(reporteRepository, masVendidoRepository, pedidosClient,
                new CriterioVenta("PAGADO,RETIRADO"), 10);
    }

    // ---------- helpers ----------

    private PedidoDto pedido(long id, LocalDate dia, String estado, int total) {
        return new PedidoDto(id, "P-" + id, dia.atTime(12, 0), estado, "WEB", total, 0, total, "12345678-5");
    }

    private DetallePedidoDto detalle(PedidoDto pedido, long idVariante, int cantidad, int subtotal) {
        return new DetallePedidoDto(null, idVariante, cantidad, subtotal / cantidad, subtotal, pedido);
    }

    /** Simula la base de datos: el reporte nuevo recibe id 1 y el ranking se devuelve tal cual. */
    private void simularGuardado() {
        when(reporteRepository.findByTipoAndFechaInicioAndFechaFin(any(), any(), any())).thenReturn(Optional.empty());
        when(reporteRepository.save(any(ReporteVentas.class))).thenAnswer(inv -> {
            ReporteVentas r = inv.getArgument(0);
            if (r.getIdReporte() == null) {
                r.setIdReporte(1L);
            }
            return r;
        });
        when(masVendidoRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private ReporteVentas reporteGuardado() {
        ArgumentCaptor<ReporteVentas> captor = ArgumentCaptor.forClass(ReporteVentas.class);
        verify(reporteRepository).save(captor.capture());
        return captor.getValue();
    }

    @SuppressWarnings("unchecked")
    private List<ProductoMasVendido> rankingGuardado() {
        ArgumentCaptor<List<ProductoMasVendido>> captor = ArgumentCaptor.forClass(List.class);
        verify(masVendidoRepository).saveAll(captor.capture());
        return captor.getValue();
    }

    // ---------- reporte diario ----------

    @Test
    @DisplayName("Diario: suma solo los pedidos del día que cuentan como venta")
    void diario_sumaSoloVentasDelDia() {
        PedidoDto p1 = pedido(1, HOY, "PAGADO", 10000);
        PedidoDto p2 = pedido(2, HOY, "RETIRADO", 5000);
        PedidoDto pendiente = pedido(3, HOY, "PENDIENTE_PAGO", 99999);
        PedidoDto ayer = pedido(4, HOY.minusDays(1), "PAGADO", 7000);
        when(pedidosClient.listarPedidos()).thenReturn(List.of(p1, p2, pendiente, ayer));
        when(pedidosClient.listarDetalles()).thenReturn(List.of());
        simularGuardado();

        ReporteDto.Detalle resultado = service.generarReporteDiario(HOY, null, "pedro");

        assertThat(resultado.reporte().tipo()).isEqualTo(ReporteVentas.Tipo.DIARIO);
        assertThat(resultado.reporte().fechaInicio()).isEqualTo(HOY);
        assertThat(resultado.reporte().fechaFin()).isEqualTo(HOY);
        assertThat(resultado.reporte().totalVentas()).isEqualTo(15000L);
        assertThat(resultado.reporte().cantidadPedidos()).isEqualTo(2);
        assertThat(resultado.reporte().generadoPor()).isEqualTo("pedro");
        assertThat(resultado.reporte().fechaGeneracion()).isNotNull();
    }

    @Test
    @DisplayName("Diario: el ranking agrupa por variante y ordena por unidades y luego por monto")
    void diario_rankingOrdenado() {
        PedidoDto p1 = pedido(1, HOY, "PAGADO", 10000);
        PedidoDto p2 = pedido(2, HOY, "RETIRADO", 5000);
        PedidoDto pendiente = pedido(3, HOY, "PENDIENTE_PAGO", 99999);
        PedidoDto ayer = pedido(4, HOY.minusDays(1), "PAGADO", 7000);
        when(pedidosClient.listarPedidos()).thenReturn(List.of(p1, p2, pendiente, ayer));
        when(pedidosClient.listarDetalles()).thenReturn(List.of(
                detalle(p1, 100, 2, 6000),
                detalle(p1, 200, 1, 4000),
                detalle(p2, 100, 1, 3000),
                detalle(p2, 300, 5, 2000),
                detalle(pendiente, 100, 50, 50000),   // pedido sin pagar: no cuenta
                detalle(ayer, 100, 9, 9000)));        // otro día: no cuenta
        simularGuardado();

        ReporteDto.Detalle resultado = service.generarReporteDiario(HOY, null, null);

        assertThat(resultado.masVendidos()).extracting(ReporteDto.MasVendido::idVariante)
                .containsExactly(300L, 100L, 200L);
        assertThat(resultado.masVendidos()).extracting(ReporteDto.MasVendido::posicion)
                .containsExactly(1, 2, 3);

        ReporteDto.MasVendido segundo = resultado.masVendidos().get(1);   // variante 100: 2 + 1 unidades
        assertThat(segundo.cantidadVendida()).isEqualTo(3);
        assertThat(segundo.totalVendido()).isEqualTo(9000L);
    }

    @Test
    @DisplayName("Diario: ante igual cantidad de unidades desempata por monto vendido")
    void diario_desempatePorMonto() {
        PedidoDto p1 = pedido(1, HOY, "PAGADO", 8000);
        when(pedidosClient.listarPedidos()).thenReturn(List.of(p1));
        when(pedidosClient.listarDetalles()).thenReturn(List.of(
                detalle(p1, 10, 2, 2000),
                detalle(p1, 20, 2, 6000)));
        simularGuardado();

        ReporteDto.Detalle resultado = service.generarReporteDiario(HOY, null, null);

        assertThat(resultado.masVendidos()).extracting(ReporteDto.MasVendido::idVariante)
                .containsExactly(20L, 10L);
    }

    @Test
    @DisplayName("Diario: 'top' limita el largo del ranking")
    void diario_topLimitaElRanking() {
        PedidoDto p1 = pedido(1, HOY, "PAGADO", 10000);
        when(pedidosClient.listarPedidos()).thenReturn(List.of(p1));
        when(pedidosClient.listarDetalles()).thenReturn(List.of(
                detalle(p1, 100, 5, 5000),
                detalle(p1, 200, 3, 3000),
                detalle(p1, 300, 1, 1000)));
        simularGuardado();

        service.generarReporteDiario(HOY, 2, null);

        assertThat(rankingGuardado()).extracting(ProductoMasVendido::getIdVariante).containsExactly(100L, 200L);
    }

    @Test
    @DisplayName("Diario: sin ventas deja total 0, ranking vacío y no pide los detalles")
    void diario_sinVentas() {
        when(pedidosClient.listarPedidos()).thenReturn(List.of(pedido(1, HOY, "PENDIENTE_PAGO", 5000)));
        simularGuardado();

        ReporteDto.Detalle resultado = service.generarReporteDiario(HOY, null, null);

        assertThat(resultado.reporte().totalVentas()).isZero();
        assertThat(resultado.reporte().cantidadPedidos()).isZero();
        assertThat(resultado.masVendidos()).isEmpty();
        verify(pedidosClient, never()).listarDetalles();
    }

    @Test
    @DisplayName("Diario: ignora pedidos sin fecha")
    void diario_pedidoSinFecha() {
        PedidoDto sinFecha = new PedidoDto(1L, "P-1", null, "PAGADO", "WEB", 1000, 0, 1000, "12345678-5");
        when(pedidosClient.listarPedidos()).thenReturn(List.of(sinFecha));
        simularGuardado();

        assertThat(service.generarReporteDiario(HOY, null, null).reporte().cantidadPedidos()).isZero();
    }

    @Test
    @DisplayName("Diario: volver a generarlo recalcula el mismo reporte y reemplaza el ranking")
    void diario_recalculaElReporteExistente() {
        ReporteVentas existente = ReporteVentas.builder().idReporte(5L).tipo(ReporteVentas.Tipo.DIARIO)
                .fechaInicio(HOY).fechaFin(HOY).totalVentas(1L).cantidadPedidos(1)
                .fechaGeneracion(LocalDateTime.now().minusDays(1)).build();
        PedidoDto p1 = pedido(1, HOY, "PAGADO", 10000);
        when(pedidosClient.listarPedidos()).thenReturn(List.of(p1));
        when(pedidosClient.listarDetalles()).thenReturn(List.of(detalle(p1, 100, 1, 10000)));
        when(reporteRepository.findByTipoAndFechaInicioAndFechaFin(ReporteVentas.Tipo.DIARIO, HOY, HOY))
                .thenReturn(Optional.of(existente));
        when(reporteRepository.save(any(ReporteVentas.class))).thenAnswer(inv -> inv.getArgument(0));
        when(masVendidoRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        ReporteDto.Detalle resultado = service.generarReporteDiario(HOY, null, null);

        assertThat(resultado.reporte().idReporte()).isEqualTo(5L);
        assertThat(resultado.reporte().totalVentas()).isEqualTo(10000L);
        verify(masVendidoRepository).deleteByIdReporte(5L);
        assertThat(rankingGuardado()).allSatisfy(p -> assertThat(p.getIdReporte()).isEqualTo(5L));
    }

    @Test
    @DisplayName("Diario: sin fecha usa el día de hoy")
    void diario_sinFechaUsaHoy() {
        when(pedidosClient.listarPedidos()).thenReturn(List.of());
        simularGuardado();

        ReporteDto.Detalle resultado = service.generarReporteDiario(null, null, null);

        assertThat(resultado.reporte().fechaInicio()).isEqualTo(LocalDate.now());
        assertThat(resultado.reporte().fechaFin()).isEqualTo(LocalDate.now());
    }

    // ---------- semanal y mensual ----------

    @Test
    @DisplayName("Semanal: va de lunes a domingo e incluye solo pedidos de esa semana")
    void semanal() {
        when(pedidosClient.listarPedidos()).thenReturn(List.of(
                pedido(1, LocalDate.of(2026, 10, 5), "PAGADO", 1000),    // lunes
                pedido(2, LocalDate.of(2026, 10, 11), "PAGADO", 2000),   // domingo
                pedido(3, LocalDate.of(2026, 10, 4), "PAGADO", 4000),    // domingo anterior: fuera
                pedido(4, LocalDate.of(2026, 10, 12), "PAGADO", 8000))); // lunes siguiente: fuera
        when(pedidosClient.listarDetalles()).thenReturn(List.of());
        simularGuardado();

        ReporteDto.Detalle resultado = service.generarReporteSemanal(HOY, null, null);

        assertThat(resultado.reporte().tipo()).isEqualTo(ReporteVentas.Tipo.SEMANAL);
        assertThat(resultado.reporte().fechaInicio()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(resultado.reporte().fechaFin()).isEqualTo(LocalDate.of(2026, 10, 11));
        assertThat(resultado.reporte().totalVentas()).isEqualTo(3000L);
    }

    @Test
    @DisplayName("Semanal: si la fecha ya es lunes, ese lunes abre la semana")
    void semanal_fechaEsLunes() {
        when(pedidosClient.listarPedidos()).thenReturn(List.of());
        simularGuardado();

        ReporteDto.Detalle resultado = service.generarReporteSemanal(LocalDate.of(2026, 10, 5), null, null);

        assertThat(resultado.reporte().fechaInicio()).isEqualTo(LocalDate.of(2026, 10, 5));
    }

    @Test
    @DisplayName("Mensual: va del día 1 al último día del mes")
    void mensual() {
        when(pedidosClient.listarPedidos()).thenReturn(List.of(
                pedido(1, LocalDate.of(2026, 2, 1), "PAGADO", 1000),
                pedido(2, LocalDate.of(2026, 2, 28), "RETIRADO", 2000),
                pedido(3, LocalDate.of(2026, 3, 1), "PAGADO", 4000)));   // fuera
        when(pedidosClient.listarDetalles()).thenReturn(List.of());
        simularGuardado();

        ReporteDto.Detalle resultado = service.generarReporteMensual(LocalDate.of(2026, 2, 15), null, null);

        assertThat(resultado.reporte().tipo()).isEqualTo(ReporteVentas.Tipo.MENSUAL);
        assertThat(resultado.reporte().fechaInicio()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(resultado.reporte().fechaFin()).isEqualTo(LocalDate.of(2026, 2, 28));   // 2026 no es bisiesto
        assertThat(resultado.reporte().totalVentas()).isEqualTo(3000L);
        assertThat(reporteGuardado().getCantidadPedidos()).isEqualTo(2);
    }

    // ---------- validaciones y errores ----------

    @Test
    @DisplayName("'top' fuera de 1..50 se rechaza con 400 sin consultar pedidos")
    void topInvalido() {
        for (int top : new int[] {0, -1, 51}) {
            assertThatThrownBy(() -> service.generarReporteDiario(HOY, top, null))
                    .isInstanceOfSatisfying(ApiException.class,
                            ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        }
        verify(pedidosClient, never()).listarPedidos();
        verify(reporteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Si el servicio de pedidos falla, el error 503 se propaga y no se guarda nada")
    void pedidosCaido() {
        when(pedidosClient.listarPedidos()).thenThrow(ApiException.servicioNoDisponible("caído"));

        assertThatThrownBy(() -> service.generarReporteDiario(HOY, null, null))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
        verify(reporteRepository, never()).save(any());
    }

    // ---------- consultas ----------

    @Test
    @DisplayName("findById: devuelve el reporte con su ranking numerado")
    void findById_ok() {
        ReporteVentas reporte = ReporteVentas.builder().idReporte(3L).tipo(ReporteVentas.Tipo.MENSUAL)
                .fechaInicio(HOY.withDayOfMonth(1)).fechaFin(HOY.withDayOfMonth(31)).totalVentas(5000L)
                .cantidadPedidos(2).fechaGeneracion(LocalDateTime.now()).build();
        when(reporteRepository.findById(3L)).thenReturn(Optional.of(reporte));
        when(masVendidoRepository.findByIdReporteOrderByCantidadVendidaDescTotalVendidoDesc(3L)).thenReturn(List.of(
                ProductoMasVendido.builder().idReporte(3L).idVariante(7L).cantidadVendida(4).totalVendido(4000L).build(),
                ProductoMasVendido.builder().idReporte(3L).idVariante(8L).cantidadVendida(1).totalVendido(1000L).build()));

        ReporteDto.Detalle detalle = service.findById(3L);

        assertThat(detalle.reporte().idReporte()).isEqualTo(3L);
        assertThat(detalle.masVendidos()).extracting(ReporteDto.MasVendido::posicion).containsExactly(1, 2);
        assertThat(detalle.masVendidos()).extracting(ReporteDto.MasVendido::idVariante).containsExactly(7L, 8L);
    }

    @Test
    @DisplayName("findById y listarMasVendidos: 404 si el reporte no existe")
    void findById_noExiste() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L)).isInstanceOfSatisfying(ApiException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> service.listarMasVendidos(99L)).isInstanceOfSatisfying(ApiException.class,
                ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("listarReportes: sin tipo trae todos y con tipo filtra")
    void listarReportes() {
        ReporteVentas diario = ReporteVentas.builder().idReporte(1L).tipo(ReporteVentas.Tipo.DIARIO)
                .fechaInicio(HOY).fechaFin(HOY).totalVentas(0L).cantidadPedidos(0)
                .fechaGeneracion(LocalDateTime.now()).build();
        when(reporteRepository.findAllByOrderByFechaInicioDesc()).thenReturn(List.of(diario));
        when(reporteRepository.findByTipoOrderByFechaInicioDesc(ReporteVentas.Tipo.SEMANAL)).thenReturn(List.of());

        assertThat(service.listarReportes(null)).hasSize(1);
        assertThat(service.listarReportes(ReporteVentas.Tipo.SEMANAL)).isEmpty();
    }
}
