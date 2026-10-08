package cl.ridertapia.reportesservice.repository;

import cl.ridertapia.reportesservice.model.DatosFactura;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatosFacturaRepository extends JpaRepository<DatosFactura, Long> {

    Optional<DatosFactura> findByIdDocumento(Long idDocumento);
}
