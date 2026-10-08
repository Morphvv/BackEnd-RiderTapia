package cl.ridertapia.reportesservice.controller;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cl.ridertapia.reportesservice.dto.ReporteDto;
import cl.ridertapia.reportesservice.exception.ApiException;
import cl.ridertapia.reportesservice.model.ReporteVentas;
import cl.ridertapia.reportesservice.service.ReporteVentasService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReporteController.class)
@Import(ReporteModelAssembler.class)
class ReporteControllerTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 10, 7);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReporteVentasService reporteService;

    private ReporteDto.Resumen resumen(ReporteVentas.Tipo tipo) {
        return new ReporteDto.Resumen(1L, tipo, FECHA, FECHA, 15000L, 2,
                LocalDateTime.of(2026, 10, 7, 18, 0), "pedro");
    }

    private ReporteDto.Detalle detalle(ReporteVentas.Tipo tipo) {
        return new ReporteDto.Detalle(resumen(tipo), List.of(
                new ReporteDto.MasVendido(1, 300L, null, 5, 2000L),
                new ReporteDto.MasVendido(2, 100L, null, 3, 9000L)));
    }

    // ---------- generar ----------

    @Test
    @DisplayName("POST /api/v1/reportes/diario → 201, Location, reporte y enlaces")
    void generarDiario() throws Exception {
        when(reporteService.generarReporteDiario(FECHA, 5, "pedro")).thenReturn(detalle(ReporteVentas.Tipo.DIARIO));

        mockMvc.perform(post("/api/v1/reportes/diario")
                        .param("fecha", "2026-10-07").param("top", "5").param("generadoPor", "pedro"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/reportes/1")))
                .andExpect(jsonPath("$.reporte.idReporte").value(1))
                .andExpect(jsonPath("$.reporte.tipo").value("DIARIO"))
                .andExpect(jsonPath("$.reporte.fechaInicio").value("2026-10-07"))
                .andExpect(jsonPath("$.reporte.totalVentas").value(15000))
                .andExpect(jsonPath("$.masVendidos", hasSize(2)))
                .andExpect(jsonPath("$.masVendidos[0].idVariante").value(300))
                .andExpect(jsonPath("$.masVendidos[0].posicion").value(1))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/reportes/1")))
                .andExpect(jsonPath("$._links.masVendidos.href", endsWith("/api/v1/reportes/1/mas-vendidos")))
                .andExpect(jsonPath("$._links.reportes.href", endsWith("/api/v1/reportes{?tipo}")));
    }

    @Test
    @DisplayName("POST /api/v1/reportes/diario sin parámetros → el servicio recibe nulos (usa hoy y valores por defecto)")
    void generarDiario_sinParametros() throws Exception {
        when(reporteService.generarReporteDiario(isNull(), isNull(), isNull()))
                .thenReturn(detalle(ReporteVentas.Tipo.DIARIO));

        mockMvc.perform(post("/api/v1/reportes/diario")).andExpect(status().isCreated());

        verify(reporteService).generarReporteDiario(isNull(), isNull(), isNull());
    }

    @Test
    @DisplayName("POST /api/v1/reportes/semanal → 201")
    void generarSemanal() throws Exception {
        when(reporteService.generarReporteSemanal(FECHA, null, null)).thenReturn(detalle(ReporteVentas.Tipo.SEMANAL));

        mockMvc.perform(post("/api/v1/reportes/semanal").param("fecha", "2026-10-07"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reporte.tipo").value("SEMANAL"));
    }

    @Test
    @DisplayName("POST /api/v1/reportes/mensual → 201")
    void generarMensual() throws Exception {
        when(reporteService.generarReporteMensual(FECHA, null, null)).thenReturn(detalle(ReporteVentas.Tipo.MENSUAL));

        mockMvc.perform(post("/api/v1/reportes/mensual").param("fecha", "2026-10-07"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reporte.tipo").value("MENSUAL"));
    }

    @Test
    @DisplayName("POST con una fecha mal escrita → 400")
    void generar_fechaInvalida() throws Exception {
        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "07-10-2026"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST con 'top' inválido → 400 con el mensaje del servicio")
    void generar_topInvalido() throws Exception {
        when(reporteService.generarReporteDiario(FECHA, 99, null))
                .thenThrow(ApiException.solicitudInvalida("El parámetro 'top' debe estar entre 1 y 50"));

        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07").param("top", "99"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parámetro 'top' debe estar entre 1 y 50"));
    }

    @Test
    @DisplayName("POST con el servicio de pedidos caído → 503")
    void generar_pedidosCaido() throws Exception {
        when(reporteService.generarReporteDiario(FECHA, null, null))
                .thenThrow(ApiException.servicioNoDisponible("No se pudo consultar el servicio de pedidos"));

        mockMvc.perform(post("/api/v1/reportes/diario").param("fecha", "2026-10-07"))
                .andExpect(status().isServiceUnavailable());
    }

    // ---------- consultar ----------

    @Test
    @DisplayName("GET /api/v1/reportes → colección HAL con enlaces por reporte")
    void listarReportes() throws Exception {
        when(reporteService.listarReportes(null)).thenReturn(List.of(
                resumen(ReporteVentas.Tipo.DIARIO), resumen(ReporteVentas.Tipo.SEMANAL)));

        mockMvc.perform(get("/api/v1/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reportes", hasSize(2)))
                .andExpect(jsonPath("$._embedded.reportes[0].tipo").value("DIARIO"))
                .andExpect(jsonPath("$._embedded.reportes[0]._links.self.href", endsWith("/api/v1/reportes/1")))
                .andExpect(jsonPath("$._embedded.reportes[0]._links.masVendidos.href").exists())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/reportes?tipo=SEMANAL → filtra por tipo")
    void listarReportes_filtrado() throws Exception {
        when(reporteService.listarReportes(ReporteVentas.Tipo.SEMANAL))
                .thenReturn(List.of(resumen(ReporteVentas.Tipo.SEMANAL)));

        mockMvc.perform(get("/api/v1/reportes").param("tipo", "SEMANAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reportes", hasSize(1)))
                .andExpect(jsonPath("$._embedded.reportes[0].tipo").value("SEMANAL"));
    }

    @Test
    @DisplayName("GET /api/v1/reportes?tipo=ANUAL → 400 (tipo no existe)")
    void listarReportes_tipoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/reportes").param("tipo", "ANUAL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/reportes sin reportes → 200 sin _embedded")
    void listarReportes_vacio() throws Exception {
        when(reporteService.listarReportes(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/reportes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/reportes/{id} → reporte con ranking y enlaces")
    void findById() throws Exception {
        when(reporteService.findById(1L)).thenReturn(detalle(ReporteVentas.Tipo.DIARIO));

        mockMvc.perform(get("/api/v1/reportes/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reporte.cantidadPedidos").value(2))
                .andExpect(jsonPath("$.masVendidos", hasSize(2)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/reportes/1")));
    }

    @Test
    @DisplayName("GET /api/v1/reportes/{id} inexistente → 404")
    void findById_noExiste() throws Exception {
        when(reporteService.findById(99L)).thenThrow(ApiException.noEncontrado("No existe el reporte 99"));

        mockMvc.perform(get("/api/v1/reportes/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("No existe el reporte 99"));
    }

    @Test
    @DisplayName("GET /api/v1/reportes/{id} con un id que no es número → 400")
    void findById_idInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/reportes/{id}", "abc")).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/reportes/{id}/mas-vendidos → solo el ranking")
    void listarMasVendidos() throws Exception {
        when(reporteService.listarMasVendidos(1L)).thenReturn(detalle(ReporteVentas.Tipo.DIARIO).masVendidos());

        mockMvc.perform(get("/api/v1/reportes/{id}/mas-vendidos", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.masVendidos", hasSize(2)))
                .andExpect(jsonPath("$._embedded.masVendidos[1].idVariante").value(100))
                .andExpect(jsonPath("$._embedded.masVendidos[1].cantidadVendida").value(3))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/v1/reportes/1/mas-vendidos")))
                .andExpect(jsonPath("$._links.reporte.href", endsWith("/api/v1/reportes/1")));
    }

    @Test
    @DisplayName("GET /api/v1/reportes/{id}/mas-vendidos de un reporte inexistente → 404")
    void listarMasVendidos_noExiste() throws Exception {
        when(reporteService.listarMasVendidos(99L)).thenThrow(ApiException.noEncontrado("No existe el reporte 99"));

        mockMvc.perform(get("/api/v1/reportes/{id}/mas-vendidos", 99)).andExpect(status().isNotFound());
    }
}
