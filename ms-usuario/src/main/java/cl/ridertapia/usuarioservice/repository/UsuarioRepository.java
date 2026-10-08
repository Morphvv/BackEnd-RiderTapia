package cl.ridertapia.usuarioservice.repository;

import cl.ridertapia.usuarioservice.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmailCliente(String emailCliente);

    boolean existsByEmailCliente(String emailCliente);
}
