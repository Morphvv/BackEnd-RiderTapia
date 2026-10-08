package cl.ridertapia.soporteservice.service;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.dto.DevolucionDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Devolucion;
import cl.ridertapia.soporteservice.repository.DevolucionRepository;
import cl.ridertapia.soporteservice.util.RutUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DevolucionService {

    // Flujo permitido: SOLICITADA -> APROBADA o RECHAZADA, y APROBADA -> COMPLETADA
    private static final Map<Devolucion.Estado, Set<Devolucion.Estado>> TRANSICIONES = Map.of(
            Devolucion.Estado.SOLICITADA, Set.of(Devolucion.Estado.APROBADA, Devolucion.Estado.RECHAZADA),
            Devolucion.Estado.APROBADA, Set.of(Devolucion.Estado.COMPLETADA),
            Devolucion.Estado.RECHAZADA, Set.of(),
            Devolucion.Estado.COMPLETADA, Set.of());

    private final DevolucionRepository devolucionRepository;
    private final UsuarioClient usuarioClient;

    @Transactional
    public DevolucionDto.Respuesta solicitarDevolucion(DevolucionDto.Crear request) {
        String rut = RutUtil.normalizar(request.rutUsuario());
        usuarioClient.obtenerPorRut(rut);

        boolean yaEnCurso = devolucionRepository.existsByIdPedidoAndIdProductoAndEstadoIn(
                request.idPedido(), request.idProducto(),
                List.of(Devolucion.Estado.SOLICITADA, Devolucion.Estado.APROBADA));
        if (yaEnCurso) {
            throw ApiException.conflicto("Ya hay una devolución en curso para el producto "
                    + request.idProducto() + " del pedido " + request.idPedido());
        }

        Devolucion devolucion = Devolucion.builder()
                .rutUsuario(rut)
                .idPedido(request.idPedido())
                .idProducto(request.idProducto())
                .motivo(request.motivo().trim())
                .estado(Devolucion.Estado.SOLICITADA)
                .fechaSolicitud(LocalDateTime.now())
                .build();
        return DevolucionDto.Respuesta.desde(devolucionRepository.save(devolucion));
    }

    /** Lista todas las devoluciones, o solo las de un estado si se indica. */
    @Transactional(readOnly = true)
    public List<DevolucionDto.Respuesta> listarDevoluciones(Devolucion.Estado estado) {
        List<Devolucion> devoluciones = estado == null
                ? devolucionRepository.findAll()
                : devolucionRepository.findByEstado(estado);
        return devoluciones.stream().map(DevolucionDto.Respuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public DevolucionDto.Respuesta findById(Long idDevolucion) {
        return DevolucionDto.Respuesta.desde(buscar(idDevolucion));
    }

    @Transactional(readOnly = true)
    public List<DevolucionDto.Respuesta> findByRutUsuario(String rutUsuario) {
        return devolucionRepository.findByRutUsuario(RutUtil.normalizar(rutUsuario))
                .stream().map(DevolucionDto.Respuesta::desde).toList();
    }

    @Transactional
    public DevolucionDto.Respuesta resolverDevolucion(Long idDevolucion, DevolucionDto.Resolver request) {
        Devolucion devolucion = buscar(idDevolucion);

        if (!TRANSICIONES.get(devolucion.getEstado()).contains(request.estado())) {
            throw ApiException.conflicto("No se puede pasar una devolución de " + devolucion.getEstado()
                    + " a " + request.estado());
        }
        if (request.estado() == Devolucion.Estado.RECHAZADA
                && (request.respuesta() == null || request.respuesta().isBlank())) {
            throw ApiException.solicitudInvalida("Debes indicar el motivo del rechazo en 'respuesta'");
        }

        devolucion.setEstado(request.estado());
        devolucion.setFechaResolucion(LocalDateTime.now());
        if (request.respuesta() != null && !request.respuesta().isBlank()) {
            devolucion.setRespuesta(request.respuesta().trim());
        }
        return DevolucionDto.Respuesta.desde(devolucionRepository.save(devolucion));
    }

    private Devolucion buscar(Long idDevolucion) {
        return devolucionRepository.findById(idDevolucion)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la devolución " + idDevolucion));
    }
}
