package cl.ridertapia.soporteservice.repository;

import cl.ridertapia.soporteservice.model.Devolucion;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DevolucionRepository extends JpaRepository<Devolucion, Long> {

    List<Devolucion> findByRutUsuario(String rutUsuario);

    List<Devolucion> findByEstado(Devolucion.Estado estado);

    boolean existsByIdPedidoAndIdProductoAndEstadoIn(Long idPedido, Long idProducto,
                                                     Collection<Devolucion.Estado> estados);
}
