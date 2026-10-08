package cl.ridertapia.soporteservice.repository;

import cl.ridertapia.soporteservice.model.Seguimiento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    List<Seguimiento> findByIdPedido(Long idPedido);
}
