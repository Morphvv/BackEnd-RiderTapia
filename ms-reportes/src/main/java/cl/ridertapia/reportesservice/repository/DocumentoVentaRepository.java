package cl.ridertapia.reportesservice.repository;

import cl.ridertapia.reportesservice.model.DocumentoVenta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentoVentaRepository extends JpaRepository<DocumentoVenta, Long> {

    boolean existsByIdPedido(Long idPedido);

    Optional<DocumentoVenta> findByIdPedido(Long idPedido);

    Optional<DocumentoVenta> findByTipoAndNumero(DocumentoVenta.Tipo tipo, Long numero);

    Optional<DocumentoVenta> findTopByTipoOrderByNumeroDesc(DocumentoVenta.Tipo tipo);

    List<DocumentoVenta> findByTipo(DocumentoVenta.Tipo tipo);

    List<DocumentoVenta> findByRutCliente(String rutCliente);
}
