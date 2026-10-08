package cl.ridertapia.soporteservice.service;

import cl.ridertapia.soporteservice.client.UsuarioClient;
import cl.ridertapia.soporteservice.dto.GarantiaDto;
import cl.ridertapia.soporteservice.exception.ApiException;
import cl.ridertapia.soporteservice.model.Garantia;
import cl.ridertapia.soporteservice.repository.GarantiaRepository;
import cl.ridertapia.soporteservice.util.RutUtil;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GarantiaService {

    private static final int MESES_POR_DEFECTO = 12;

    private final GarantiaRepository garantiaRepository;
    private final UsuarioClient usuarioClient;

    @Transactional
    public GarantiaDto.Respuesta registrarGarantia(GarantiaDto.Crear request) {
        String rut = RutUtil.normalizar(request.rutUsuario());
        usuarioClient.obtenerPorRut(rut);

        if (garantiaRepository.existsByIdPedidoAndIdProducto(request.idPedido(), request.idProducto())) {
            throw ApiException.conflicto("Ya existe una garantía para el producto " + request.idProducto()
                    + " del pedido " + request.idPedido());
        }

        int meses = request.mesesCobertura() == null ? MESES_POR_DEFECTO : request.mesesCobertura();
        LocalDate hoy = LocalDate.now();

        Garantia garantia = Garantia.builder()
                .rutUsuario(rut)
                .idPedido(request.idPedido())
                .idProducto(request.idProducto())
                .fechaInicio(hoy)
                .fechaTermino(hoy.plusMonths(meses))
                .estado(Garantia.Estado.VIGENTE)
                .build();
        return GarantiaDto.Respuesta.desde(garantiaRepository.save(garantia));
    }

    @Transactional(readOnly = true)
    public List<GarantiaDto.Respuesta> listarGarantias() {
        return garantiaRepository.findAll().stream().map(GarantiaDto.Respuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public GarantiaDto.Respuesta findById(Long idGarantia) {
        return GarantiaDto.Respuesta.desde(buscar(idGarantia));
    }

    @Transactional(readOnly = true)
    public List<GarantiaDto.Respuesta> findByRutUsuario(String rutUsuario) {
        return garantiaRepository.findByRutUsuario(RutUtil.normalizar(rutUsuario))
                .stream().map(GarantiaDto.Respuesta::desde).toList();
    }

    /** Indica si la garantía sigue vigente hoy y, si ya venció, la marca como EXPIRADA. */
    @Transactional
    public GarantiaDto.Vigencia verificarVigencia(Long idGarantia) {
        Garantia garantia = buscar(idGarantia);
        LocalDate hoy = LocalDate.now();
        boolean vigente = !hoy.isAfter(garantia.getFechaTermino());

        if (!vigente && garantia.getEstado() == Garantia.Estado.VIGENTE) {
            garantia.setEstado(Garantia.Estado.EXPIRADA);
            garantiaRepository.save(garantia);
        }

        long diasRestantes = vigente ? ChronoUnit.DAYS.between(hoy, garantia.getFechaTermino()) : 0;
        return new GarantiaDto.Vigencia(garantia.getIdGarantia(), vigente, garantia.getFechaTermino(), diasRestantes);
    }

    private Garantia buscar(Long idGarantia) {
        return garantiaRepository.findById(idGarantia)
                .orElseThrow(() -> ApiException.noEncontrado("No existe la garantía " + idGarantia));
    }
}
