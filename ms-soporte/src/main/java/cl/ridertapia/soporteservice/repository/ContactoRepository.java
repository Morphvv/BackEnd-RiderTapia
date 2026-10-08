package cl.ridertapia.soporteservice.repository;

import cl.ridertapia.soporteservice.model.Contacto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    List<Contacto> findByEstado(Contacto.Estado estado);
}
