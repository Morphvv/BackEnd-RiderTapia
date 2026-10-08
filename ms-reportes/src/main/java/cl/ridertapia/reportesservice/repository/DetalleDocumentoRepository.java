package cl.ridertapia.reportesservice.repository;

import cl.ridertapia.reportesservice.model.DetalleDocumento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleDocumentoRepository extends JpaRepository<DetalleDocumento, Long> {

    List<DetalleDocumento> findByIdDocumento(Long idDocumento);
}
