package cl.ridertapia.reportesservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.reportesservice.client.DetallePedidoDto;
import cl.ridertapia.reportesservice.client.PedidoDto;
import cl.ridertapia.reportesservice.client.PedidosClient;
import cl.ridertapia.reportesservice.exception.ApiException;
import cl.ridertapia.reportesservice.model.ReporteVentas;
import cl.ridertapia.reportesservice.repository.ProductoMasVendidoRepository;
import cl.ridertapia.reportesservice.repository.ReporteVentasRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class ReporteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReporteVentasRepository reporteRepository;

    @Autowired
    private ProductoMasVendidoRepository masVendidoRepository;

    @MockBean
    private PedidosClient pedidosClient;

    private PedidoDto p1;
    private PedidoDto p2;

    @BeforeEach
    void preparar() {
        masVendidoRepository.deleteAll();
        reporteRepository.deleteAll();

        // Miércoles 7 de octubre de 2026: dos ventas, un pedido pendiente y una venta de otro día
        p1 = new PedidoDto(1L, "P-1", LocalDateTime.of(2026, 10, 7, 10, 0), "PAGADO", "WEB", 10000, 0, 10000, "12345678-5");
        p2 = new PedidoDto(2L, "P-2", LocalDateTime.of(2026, 10, 7, 15, 0), "RETIRADO", "WEB", 5000, 0, 5000, "11111111-1");
        PedidoDto pendiente = new PedidoDto(3L, "P-3", LocalDateTime.of(2026, 10, 7, 16, 0), "PENDIENTE_PAGO", "WEB", 99999, 0, 99999, "22222222-2");
        PedidoDto ayer = new PedidoDto(4L, "P-4", LocalDateTime.of(2026, 10, 6, 10, 0), "PAGADO", "WEB", 7000, 0, 7000, "12345678-5");
        when(pedidosClient.listarPedidos()).thenReturn(List.of(p1, p2, pendiente, ayer));
        when(pedidosClient.listarDetalles()).thenReturn(List.of(
                new DetallePedidoDto(1L, 100L, 2, 3000, 6000, p1),
                new DetallePedidoDto(2L, 200L, 1, 4000, 4000, p1),
                new DetallePedidoDto(3L, 100L, 1, 3000, 3000, p2),
                new DetallePedidoDto(4L, 300L, 5, 400, 2000, p2),
                new DetallePedidoDto(5L, 100L, 50, 1000, 50000, pendiente),
                new DetallePedidoDto(6L, 100L, 9, 1000, 9000, ayer)));
    }

    @Test
    void testGenerarReporteDiarioYObtenerlo() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/diario")
                        .param("fecha", "2026-10-07").param("generadoPor", "pedro"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reporte.idReporte").exists())
                .andExpect(jsonPath("$.reporte.tipo").value("DIARIO"))
                .andExpect(jsonPath("$.reporte.totalVentas").value(15000))
                .andExpect(jsonPath("$.reporte.cantidadPedidos").value(2))
                .andExpect(jsonPath("$.reporte.generadoPor").value("pedro"))
                .andExpect(jsonPath("$.masVendidos", hasSize(3)))
                .andExpect(jsonPath("$.masVendidos[0].idVariante").value(300))   // 5 unidades
                .andExpect(jsonPath("$.masVendidos[1].idVariante").value(100))   // 3 unidades
                .andExpect(jsonPath("$.masVendidos[2].idVariante").value(200))   // 1 unidad
                .andExpect(jsonPath("$._links").exists());

        Long id = reporteRepository.findAll().get(0).getIdReporte();

        mockMvc.perform(get("/api/v1/reportes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reporte.totalVentas").value(15000))
                .andExpect(jsonPath("$.masVendidos", hasSize(3)))
                .andExpect(jsonPath("$._links.self").exists());

        mockMvc.perform(get("/api/v1/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reportes", hasSize(1)))
                .andExpect(jsonPath("$._embedded.reportes[0].tipo").value("DIARIO"))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void testObtenerSoloElRanking() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07"))
                .andExpect(status().isCreated());
        Long id = reporteRepository.findAll().get(0).getIdReporte();

        mockMvc.perform(get("/api/v1/reportes/" + id + "/mas-vendidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.masVendidos", hasSize(3)))
                .andExpect(jsonPath("$._embedded.masVendidos[1].idVariante").value(100))
                .andExpect(jsonPath("$._embedded.masVendidos[1].cantidadVendida").value(3))
                .andExpect(jsonPath("$._embedded.masVendidos[1].totalVendido").value(9000))
                .andExpect(jsonPath("$._links.reporte").exists());
    }

    @Test
    void testTopLimitaElRanking() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07").param("top", "2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.masVendidos", hasSize(2)))
                .andExpect(jsonPath("$.masVendidos[0].idVariante").value(300))
                .andExpect(jsonPath("$.masVendidos[1].idVariante").value(100));
    }

    @Test
    void testVolverAGenerarNoDuplicaElReporte() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07"))
                .andExpect(status().isCreated());

        // Llega una venta más y se vuelve a generar el mismo día
        PedidoDto nueva = new PedidoDto(5L, "P-5", LocalDateTime.of(2026, 10, 7, 18, 0), "PAGADO", "WEB", 2000, 0, 2000, "33333333-3");
        when(pedidosClient.listarPedidos()).thenReturn(List.of(p1, p2, nueva));
        when(pedidosClient.listarDetalles()).thenReturn(List.of(
                new DetallePedidoDto(1L, 100L, 2, 3000, 6000, p1),
                new DetallePedidoDto(7L, 400L, 20, 100, 2000, nueva)));

        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reporte.totalVentas").value(17000))
                .andExpect(jsonPath("$.reporte.cantidadPedidos").value(3))
                .andExpect(jsonPath("$.masVendidos", hasSize(2)));

        assertThat(reporteRepository.count()).isEqualTo(1);
        Long id = reporteRepository.findAll().get(0).getIdReporte();
        // El ranking viejo se reemplazó: no quedan filas duplicadas
        assertThat(masVendidoRepository.findByIdReporteOrderByCantidadVendidaDescTotalVendidoDesc(id)).hasSize(2);
    }

    @Test
    void testReporteSemanal() throws Exception {
        PedidoDto lunes = new PedidoDto(6L, "P-6", LocalDateTime.of(2026, 10, 5, 9, 0), "LISTO_PARA_RETIRO", "WEB", 1000, 0, 1000, "12345678-5");
        PedidoDto domingoAnterior = new PedidoDto(7L, "P-7", LocalDateTime.of(2026, 10, 4, 9, 0), "PAGADO", "WEB", 8000, 0, 8000, "12345678-5");
        when(pedidosClient.listarPedidos()).thenReturn(List.of(p1, p2, lunes, domingoAnterior));
        when(pedidosClient.listarDetalles()).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/reportes/semanal").param("fecha", "2026-10-07"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reporte.tipo").value("SEMANAL"))
                .andExpect(jsonPath("$.reporte.fechaInicio").value("2026-10-05"))
                .andExpect(jsonPath("$.reporte.fechaFin").value("2026-10-11"))
                .andExpect(jsonPath("$.reporte.totalVentas").value(16000))
                .andExpect(jsonPath("$.reporte.cantidadPedidos").value(3));
    }

    @Test
    void testReporteMensual() throws Exception {
        when(pedidosClient.listarDetalles()).thenReturn(List.of());

        mockMvc.perform(post("/api/v1/reportes/mensual").param("fecha", "2026-10-15"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reporte.tipo").value("MENSUAL"))
                .andExpect(jsonPath("$.reporte.fechaInicio").value("2026-10-01"))
                .andExpect(jsonPath("$.reporte.fechaFin").value("2026-10-31"))
                .andExpect(jsonPath("$.reporte.totalVentas").value(22000));   // 10000 + 5000 + 7000 (ayer)
    }

    @Test
    void testListarReportesFiltradoPorTipo() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07")).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/reportes/mensual").param("fecha", "2026-10-07")).andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/reportes"))
                .andExpect(jsonPath("$._embedded.reportes", hasSize(2)));
        mockMvc.perform(get("/api/v1/reportes").param("tipo", "MENSUAL"))
                .andExpect(jsonPath("$._embedded.reportes", hasSize(1)))
                .andExpect(jsonPath("$._embedded.reportes[0].tipo").value("MENSUAL"));
        mockMvc.perform(get("/api/v1/reportes").param("tipo", "SEMANAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    void testDiaSinVentas() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-01-01"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reporte.totalVentas").value(0))
                .andExpect(jsonPath("$.reporte.cantidadPedidos").value(0))
                .andExpect(jsonPath("$.masVendidos", hasSize(0)));
    }

    @Test
    void testObtenerReporteInexistente() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(get("/api/v1/reportes/9999/mas-vendidos")).andExpect(status().isNotFound());
    }

    @Test
    void testParametrosInvalidos() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "07/10/2026"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07").param("top", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07").param("top", "51"))
                .andExpect(status().isBadRequest());

        assertThat(reporteRepository.count()).isZero();
    }

    @Test
    void testServicioDePedidosCaido() throws Exception {
        when(pedidosClient.listarPedidos()).thenThrow(ApiException.servicioNoDisponible("pedidos caído"));

        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07"))
                .andExpect(status().isServiceUnavailable());

        assertThat(reporteRepository.count()).isZero();
    }

    @Test
    void testTipoDeReporteGuardadoEnBd() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07")).andExpect(status().isCreated());

        ReporteVentas enBd = reporteRepository.findAll().get(0);
        assertThat(enBd.getTipo()).isEqualTo(ReporteVentas.Tipo.DIARIO);
        assertThat(enBd.getTotalVentas()).isEqualTo(15000L);
        assertThat(enBd.getFechaGeneracion()).isNotNull();
    }
}
