package cl.ridertapia.soporteservice.service;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.dto.ResenaDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Resena;
import cl.ridertapia.soporteservice.repository.ResenaRepository;
import cl.ridertapia.soporteservice.util.RutUtil;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final UsuarioClient usuarioClient;

    @Transactional
    public ResenaDto.Respuesta guardarResena(ResenaDto.Crear request) {
        String rut = RutUtil.normalizar(request.rutUsuario());
        usuarioClient.obtenerPorRut(rut);

        if (resenaRepository.existsByRutUsuarioAndIdProducto(rut, request.idProducto())) {
            throw ApiException.conflicto("Ya publicaste una reseña para el producto " + request.idProducto());
        }

        Resena resena = Resena.builder()
                .rutUsuario(rut)
                .idProducto(request.idProducto())
                .calificacion(request.calificacion())
                .comentario(request.comentario())
                .fecha(LocalDateTime.now())
                .estado(Resena.Estado.VISIBLE)
                .build();
        return ResenaDto.Respuesta.desde(resenaRepository.save(resena));
    }

    /** Todas las reseñas, incluidas las ocultas (uso del administrador). */
    @Transactional(readOnly = true)
    public List<ResenaDto.Respuesta> listarResenas() {
        return resenaRepository.findAll().stream().map(ResenaDto.Respuesta::desde).toList();
    }

    /** Reseñas visibles de un producto (lo que ve el público). */
    @Transactional(readOnly = true)
    public List<ResenaDto.Respuesta> findByProducto(Long idProducto) {
        return resenaRepository.findByIdProductoAndEstado(idProducto, Resena.Estado.VISIBLE)
                .stream().map(ResenaDto.Respuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public ResenaDto.Resumen resumenProducto(Long idProducto) {
        List<Resena> visibles = resenaRepository.findByIdProductoAndEstado(idProducto, Resena.Estado.VISIBLE);
        double promedio = visibles.stream()
                .mapToInt(Resena::getCalificacion)
                .average()
                .orElse(0.0);
        double redondeado = Math.round(promedio * 10.0) / 10.0;
        return new ResenaDto.Resumen(idProducto, redondeado, visibles.size());
    }

    @Transactional(readOnly = true)
    public List<ResenaDto.Respuesta> findByRutUsuario(String rutUsuario) {
        return resenaRepository.findByRutUsuario(RutUtil.normalizar(rutUsuario))
                .stream().map(ResenaDto.Respuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public ResenaDto.Respuesta findById(Long idResena) {
        return ResenaDto.Respuesta.desde(buscar(idResena));
    }

    @Transactional
    public ResenaDto.Respuesta ocultarResena(Long idResena) {
        Resena resena = buscar(idResena);
        resena.setEstado(Resena.Estado.OCULTA);
        return ResenaDto.Respuesta.desde(resenaRepository.save(resena));
    }

    private Resena buscar(Long idResena) {
        return resenaRepository.findById(idResena)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la reseña " + idResena));
    }
}
