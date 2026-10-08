package cl.ridertapia.soporteservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cl.ridertapia.soporteservice.model.Contacto;
import cl.ridertapia.soporteservice.model.Devolucion;
import cl.ridertapia.soporteservice.model.Garantia;
import cl.ridertapia.soporteservice.model.Resena;
import cl.ridertapia.soporteservice.model.Seguimiento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class SoporteRepositoriesTest {

    @Autowired
    private ResenaRepository resenaRepository;
    @Autowired
    private GarantiaRepository garantiaRepository;
    @Autowired
    private DevolucionRepository devolucionRepository;
    @Autowired
    private ContactoRepository contactoRepository;
    @Autowired
    private SeguimientoRepository seguimientoRepository;

    private Resena resena(String rut, Long producto, Resena.Estado estado) {
        return Resena.builder().rutUsuario(rut).idProducto(producto).calificacion(5).comentario("ok")
                .fecha(LocalDateTime.now()).estado(estado).build();
    }

    private Devolucion devolucion(String rut, Long pedido, Long producto, Devolucion.Estado estado) {
        return Devolucion.builder().rutUsuario(rut).idPedido(pedido).idProducto(producto)
                .motivo("Llegó con la llanta dañada").estado(estado).fechaSolicitud(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("Reseña: un usuario no puede reseñar dos veces el mismo producto (restricción única)")
    void resena_unica() {
        resenaRepository.saveAndFlush(resena("12345678-5", 100L, Resena.Estado.VISIBLE));

        assertThatThrownBy(() -> resenaRepository.saveAndFlush(resena("12345678-5", 100L, Resena.Estado.VISIBLE)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Reseña: el mismo usuario sí puede reseñar otro producto")
    void resena_otroProducto() {
        resenaRepository.save(resena("12345678-5", 100L, Resena.Estado.VISIBLE));
        resenaRepository.saveAndFlush(resena("12345678-5", 101L, Resena.Estado.VISIBLE));

        assertThat(resenaRepository.existsByRutUsuarioAndIdProducto("12345678-5", 100L)).isTrue();
        assertThat(resenaRepository.existsByRutUsuarioAndIdProducto("12345678-5", 999L)).isFalse();
        assertThat(resenaRepository.findByRutUsuario("12345678-5")).hasSize(2);
    }

    @Test
    @DisplayName("Reseña: findByIdProductoAndEstado deja fuera las ocultas")
    void resena_soloVisibles() {
        resenaRepository.save(resena("12345678-5", 100L, Resena.Estado.VISIBLE));
        resenaRepository.save(resena("11111111-1", 100L, Resena.Estado.OCULTA));
        resenaRepository.save(resena("10000013-K", 200L, Resena.Estado.VISIBLE));

        List<Resena> visibles = resenaRepository.findByIdProductoAndEstado(100L, Resena.Estado.VISIBLE);

        assertThat(visibles).hasSize(1);
        assertThat(visibles.get(0).getRutUsuario()).isEqualTo("12345678-5");
    }

    @Test
    @DisplayName("Garantía: existsByIdPedidoAndIdProducto y findByRutUsuario")
    void garantia_consultas() {
        garantiaRepository.save(Garantia.builder().rutUsuario("12345678-5").idPedido(1L).idProducto(100L)
                .fechaInicio(LocalDate.now()).fechaTermino(LocalDate.now().plusMonths(12))
                .estado(Garantia.Estado.VIGENTE).build());

        assertThat(garantiaRepository.existsByIdPedidoAndIdProducto(1L, 100L)).isTrue();
        assertThat(garantiaRepository.existsByIdPedidoAndIdProducto(1L, 101L)).isFalse();
        assertThat(garantiaRepository.findByRutUsuario("12345678-5")).hasSize(1);
        assertThat(garantiaRepository.findByRutUsuario("11111111-1")).isEmpty();
    }

    @Test
    @DisplayName("Devolución: solo cuentan como 'en curso' SOLICITADA y APROBADA")
    void devolucion_enCurso() {
        devolucionRepository.save(devolucion("12345678-5", 1L, 100L, Devolucion.Estado.RECHAZADA));
        devolucionRepository.save(devolucion("12345678-5", 2L, 100L, Devolucion.Estado.APROBADA));
        List<Devolucion.Estado> enCurso = List.of(Devolucion.Estado.SOLICITADA, Devolucion.Estado.APROBADA);

        assertThat(devolucionRepository.existsByIdPedidoAndIdProductoAndEstadoIn(1L, 100L, enCurso)).isFalse();
        assertThat(devolucionRepository.existsByIdPedidoAndIdProductoAndEstadoIn(2L, 100L, enCurso)).isTrue();
    }

    @Test
    @DisplayName("Devolución: findByEstado y findByRutUsuario")
    void devolucion_filtros() {
        devolucionRepository.save(devolucion("12345678-5", 1L, 100L, Devolucion.Estado.SOLICITADA));
        devolucionRepository.save(devolucion("11111111-1", 2L, 101L, Devolucion.Estado.COMPLETADA));

        assertThat(devolucionRepository.findByEstado(Devolucion.Estado.SOLICITADA)).hasSize(1);
        assertThat(devolucionRepository.findByRutUsuario("11111111-1")).hasSize(1);
        assertThat(devolucionRepository.findByRutUsuario("10000013-K")).isEmpty();
    }

    @Test
    @DisplayName("Contacto: el RUT es opcional y findByEstado filtra")
    void contacto_filtros() {
        contactoRepository.save(Contacto.builder().nombre("Juan").emailContacto("juan@example.com").asunto("Duda")
                .mensaje("Hola").estado(Contacto.Estado.ABIERTO).fecha(LocalDateTime.now()).build());
        contactoRepository.save(Contacto.builder().rutUsuario("12345678-5").nombre("Ana").emailContacto("ana@example.com")
                .asunto("Otra").mensaje("Hola").estado(Contacto.Estado.RESPONDIDO).fecha(LocalDateTime.now()).build());

        assertThat(contactoRepository.findByEstado(Contacto.Estado.ABIERTO)).hasSize(1);
        assertThat(contactoRepository.findByEstado(Contacto.Estado.ABIERTO).get(0).getRutUsuario()).isNull();
        assertThat(contactoRepository.findByEstado(Contacto.Estado.RESPONDIDO)).hasSize(1);
    }

    @Test
    @DisplayName("Seguimiento: findByIdPedido")
    void seguimiento_porPedido() {
        seguimientoRepository.save(Seguimiento.builder().idPedido(1L).rutUsuario("12345678-5").tipo(Seguimiento.Tipo.LLAMADA)
                .estado(Seguimiento.Estado.PENDIENTE).fecha(LocalDateTime.now()).build());
        seguimientoRepository.save(Seguimiento.builder().idPedido(2L).rutUsuario("12345678-5").tipo(Seguimiento.Tipo.CORREO)
                .estado(Seguimiento.Estado.PENDIENTE).fecha(LocalDateTime.now()).build());

        assertThat(seguimientoRepository.findByIdPedido(1L)).hasSize(1);
        assertThat(seguimientoRepository.findByIdPedido(99L)).isEmpty();
    }
}
