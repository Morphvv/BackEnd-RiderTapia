package cl.ridertapia.soporteservice.repository;

import cl.ridertapia.soporteservice.model.Garantia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarantiaRepository extends JpaRepository<Garantia, Long> {

    List<Garantia> findByRutUsuario(String rutUsuario);

    boolean existsByIdPedidoAndIdProducto(Long idPedido, Long idProducto);
}
