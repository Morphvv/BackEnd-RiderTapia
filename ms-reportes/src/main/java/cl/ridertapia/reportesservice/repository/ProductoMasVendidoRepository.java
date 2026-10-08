package cl.ridertapia.reportesservice.repository;

import cl.ridertapia.reportesservice.model.ProductoMasVendido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoMasVendidoRepository extends JpaRepository<ProductoMasVendido, Long> {

    List<ProductoMasVendido> findByIdReporteOrderByCantidadVendidaDescTotalVendidoDesc(Long idReporte);

    void deleteByIdReporte(Long idReporte);
}
