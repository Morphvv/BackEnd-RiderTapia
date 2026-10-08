package cl.ridertapia.soporteservice.service;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.dto.SeguimientoDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Seguimiento;
import cl.ridertapia.soporteservice.repository.SeguimientoRepository;
import cl.ridertapia.soporteservice.util.RutUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final UsuarioClient usuarioClient;

    @Transactional
    public SeguimientoDto.Respuesta registrarSeguimiento(SeguimientoDto.Crear request) {
        String rut = RutUtil.normalizar(request.rutUsuario());
        usuarioClient.obtenerPorRut(rut);

        Seguimiento seguimiento = Seguimiento.builder()
                .idPedido(request.idPedido())
                .rutUsuario(rut)
                .tipo(request.tipo())
                .estado(Seguimiento.Estado.PENDIENTE)
                .observacion(request.observacion())
                .fecha(LocalDateTime.now())
                .build();
        return SeguimientoDto.Respuesta.desde(seguimientoRepository.save(seguimiento));
    }

    @Transactional(readOnly = true)
    public List<SeguimientoDto.Respuesta> listarSeguimientos() {
        return seguimientoRepository.findAll().stream().map(SeguimientoDto.Respuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<SeguimientoDto.Respuesta> findByPedido(Long idPedido) {
        return seguimientoRepository.findByIdPedido(idPedido)
                .stream().map(SeguimientoDto.Respuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public SeguimientoDto.Respuesta findById(Long idSeguimiento) {
        return SeguimientoDto.Respuesta.desde(buscar(idSeguimiento));
    }

    @Transactional
    public SeguimientoDto.Respuesta actualizarEstado(Long idSeguimiento, SeguimientoDto.ActualizarEstado request) {
        Seguimiento seguimiento = buscar(idSeguimiento);
        seguimiento.setEstado(request.estado());
        if (request.observacion() != null && !request.observacion().isBlank()) {
            seguimiento.setObservacion(request.observacion().trim());
        }
        return SeguimientoDto.Respuesta.desde(seguimientoRepository.save(seguimiento));
    }

    private Seguimiento buscar(Long idSeguimiento) {
        return seguimientoRepository.findById(idSeguimiento)
                .orElseThrow(() -> ApiException.noEncontrado("No existe el seguimiento " + idSeguimiento));
    }
}
