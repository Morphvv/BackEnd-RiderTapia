package cl.ridertapia.soporteservice.repository;

import cl.ridertapia.soporteservice.model.Resena;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    boolean existsByRutUsuarioAndIdProducto(String rutUsuario, Long idProducto);

    List<Resena> findByIdProductoAndEstado(Long idProducto, Resena.Estado estado);

    List<Resena> findByRutUsuario(String rutUsuario);
}
