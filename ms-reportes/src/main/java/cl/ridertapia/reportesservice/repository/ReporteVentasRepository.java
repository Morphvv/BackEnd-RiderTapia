package cl.ridertapia.reportesservice.repository;

import cl.ridertapia.reportesservice.model.ReporteVentas;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReporteVentasRepository extends JpaRepository<ReporteVentas, Long> {

    Optional<ReporteVentas> findByTipoAndFechaInicioAndFechaFin(ReporteVentas.Tipo tipo,
                                                                LocalDate fechaInicio, LocalDate fechaFin);

    List<ReporteVentas> findByTipoOrderByFechaInicioDesc(ReporteVentas.Tipo tipo);

    List<ReporteVentas> findAllByOrderByFechaInicioDesc();
}
