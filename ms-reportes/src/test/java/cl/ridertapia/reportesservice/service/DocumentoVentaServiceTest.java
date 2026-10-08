package cl.ridertapia.reportesservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.ridertapia.reportesservice.client.DetallePedidoDto;
import cl.ridertapia.reportesservice.client.PedidoDto;
import cl.ridertapia.reportesservice.client.PedidosClient;
import cl.ridertapia.reportesservice.dto.DocumentoDto;
import cl.ridertapia.reportesservice.exception.ApiException;
import cl.ridertapia.reportesservice.model.DatosFactura;
import cl.ridertapia.reportesservice.model.DetalleDocumento;
import cl.ridertapia.reportesservice.model.DocumentoVenta;
import cl.ridertapia.reportesservice.repository.DatosFacturaRepository;
import cl.ridertapia.reportesservice.repository.DetalleDocumentoRepository;
import cl.ridertapia.reportesservice.repository.DocumentoVentaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class DocumentoVentaServiceTest {

    @Mock
    private DocumentoVentaRepository documentoRepository;

    @Mock
    private DetalleDocumentoRepository detalleRepository;

    @Mock
    private DatosFacturaRepository datosFacturaRepository;

    @Mock
    private PedidosClient pedidosClient;

    private DocumentoVentaService service;

    @BeforeEach
    void preparar() {
        service = new DocumentoVentaService(documentoRepository, detalleRepository, datosFacturaRepository,
                pedidosClient, new CriterioVenta("PAGADO,RETIRADO"));
    }

    // ---------- helpers ----------

    private PedidoDto pedido(String estado, Integer total, Integer descuento) {
        return new PedidoDto(1L, "P-1", LocalDateTime.of(2026, 10, 7, 12, 0), estado, "WEB",
                total, descuento, total, "12345678-5");
    }

    private DetallePedidoDto linea(long idVariante, int cantidad, int precio) {
        return new DetallePedidoDto(null, idVariante, cantidad, precio, cantidad * precio, null);
    }

    /** Simula la base de datos: asigna id 1 al documento y devuelve lo mismo que recibe. */
    private void simularGuardado() {
        when(documentoRepository.save(any(DocumentoVenta.class))).thenAnswer(inv -> {
            DocumentoVenta d = inv.getArgument(0);
            d.setIdDocumento(1L);
            return d;
        });
        when(detalleRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void pedidoPagado(int total, int descuento, DetallePedidoDto... lineas) {
        when(documentoRepository.existsByIdPedido(1L)).thenReturn(false);
        when(pedidosClient.obtenerPedido(1L)).thenReturn(pedido("PAGADO", total, descuento));
        when(pedidosClient.listarDetallesPorPedido(1L)).thenReturn(List.of(lineas));
        when(documentoRepository.findTopByTipoOrderByNumeroDesc(any())).thenReturn(Optional.empty());
        simularGuardado();
    }

    private void assertEstado(Throwable ex, HttpStatus esperado) {
        assertThat(ex).isInstanceOf(ApiException.class);
        assertThat(((ApiException) ex).getStatus()).isEqualTo(esperado);
    }

    // ---------- boleta ----------

    @Test
    @DisplayName("Boleta: separa neto e IVA (19%) a partir del total, que ya incluye IVA")
    void boleta_calculaIva() {
        pedidoPagado(11900, 0, linea(100, 2, 5950));

        DocumentoDto.Detalle detalle = service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L));

        assertThat(detalle.documento().tipo()).isEqualTo(DocumentoVenta.Tipo.BOLETA);
        assertThat(detalle.documento().numero()).isEqualTo(1L);
        assertThat(detalle.documento().idPedido()).isEqualTo(1L);
        assertThat(detalle.documento().rutCliente()).isEqualTo("12345678-5");
        assertThat(detalle.documento().total()).isEqualTo(11900);
        assertThat(detalle.documento().neto()).isEqualTo(10000);
        assertThat(detalle.documento().iva()).isEqualTo(1900);
        assertThat(detalle.datosFactura()).isNull();
        verify(datosFacturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Boleta: con redondeo, neto + IVA siempre suma exactamente el total")
    void boleta_redondeo() {
        pedidoPagado(10000, 0, linea(100, 1, 10000));

        DocumentoDto.Resumen doc = service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L)).documento();

        assertThat(doc.neto()).isEqualTo(8403);
        assertThat(doc.iva()).isEqualTo(1597);
        assertThat(doc.neto() + doc.iva()).isEqualTo(doc.total());
    }

    @Test
    @DisplayName("Boleta: copia las líneas del pedido")
    void boleta_lineas() {
        pedidoPagado(30000, 0, linea(100, 2, 10000), linea(200, 1, 10000));

        DocumentoDto.Detalle detalle = service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L));

        assertThat(detalle.lineas()).hasSize(2);
        DocumentoDto.Linea primera = detalle.lineas().get(0);
        assertThat(primera.idVariante()).isEqualTo(100L);
        assertThat(primera.descripcion()).isEqualTo("Variante 100");
        assertThat(primera.cantidad()).isEqualTo(2);
        assertThat(primera.precioUnitario()).isEqualTo(10000);
        assertThat(primera.subtotal()).isEqualTo(20000);
    }

    @Test
    @DisplayName("Boleta: un descuento se agrega como línea negativa para que las líneas sumen el total")
    void boleta_conDescuento() {
        pedidoPagado(9000, 1000, linea(100, 1, 10000));

        DocumentoDto.Detalle detalle = service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L));

        assertThat(detalle.lineas()).hasSize(2);
        DocumentoDto.Linea descuento = detalle.lineas().get(1);
        assertThat(descuento.descripcion()).isEqualTo("Descuento aplicado");
        assertThat(descuento.idVariante()).isNull();
        assertThat(descuento.subtotal()).isEqualTo(-1000);
        assertThat(detalle.lineas().stream().mapToInt(DocumentoDto.Linea::subtotal).sum())
                .isEqualTo(detalle.documento().total());
    }

    @Test
    @DisplayName("Boleta: el número es correlativo dentro de su tipo")
    void boleta_correlativo() {
        when(documentoRepository.existsByIdPedido(1L)).thenReturn(false);
        when(pedidosClient.obtenerPedido(1L)).thenReturn(pedido("RETIRADO", 5000, 0));
        when(pedidosClient.listarDetallesPorPedido(1L)).thenReturn(List.of(linea(100, 1, 5000)));
        when(documentoRepository.findTopByTipoOrderByNumeroDesc(DocumentoVenta.Tipo.BOLETA))
                .thenReturn(Optional.of(DocumentoVenta.builder().numero(7L).build()));
        simularGuardado();

        assertThat(service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L)).documento().numero()).isEqualTo(8L);
    }

    @Test
    @DisplayName("Boleta: 409 si el pedido ya tiene documento, sin consultar pedidos")
    void boleta_pedidoYaFacturado() {
        when(documentoRepository.existsByIdPedido(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L)))
                .satisfies(ex -> assertEstado(ex, HttpStatus.CONFLICT));
        verify(pedidosClient, never()).obtenerPedido(any());
        verify(documentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Boleta: 400 si el pedido no está en un estado de venta")
    void boleta_pedidoSinPagar() {
        when(documentoRepository.existsByIdPedido(1L)).thenReturn(false);
        when(pedidosClient.obtenerPedido(1L)).thenReturn(pedido("PENDIENTE_PAGO", 5000, 0));

        assertThatThrownBy(() -> service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L)))
                .satisfies(ex -> assertEstado(ex, HttpStatus.BAD_REQUEST))
                .hasMessageContaining("PENDIENTE_PAGO");
        verify(documentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Boleta: 400 si el pedido no tiene total válido")
    void boleta_totalInvalido() {
        when(documentoRepository.existsByIdPedido(1L)).thenReturn(false);
        when(pedidosClient.obtenerPedido(1L)).thenReturn(pedido("PAGADO", 0, 0));

        assertThatThrownBy(() -> service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L)))
                .satisfies(ex -> assertEstado(ex, HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("Boleta: 400 si el pedido no tiene productos")
    void boleta_sinDetalles() {
        when(documentoRepository.existsByIdPedido(1L)).thenReturn(false);
        when(pedidosClient.obtenerPedido(1L)).thenReturn(pedido("PAGADO", 5000, 0));
        when(pedidosClient.listarDetallesPorPedido(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L)))
                .satisfies(ex -> assertEstado(ex, HttpStatus.BAD_REQUEST));
        verify(documentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Boleta: 404 si el pedido no existe")
    void boleta_pedidoInexistente() {
        when(documentoRepository.existsByIdPedido(1L)).thenReturn(false);
        when(pedidosClient.obtenerPedido(1L)).thenThrow(ApiException.noEncontrado("No existe el pedido 1"));

        assertThatThrownBy(() -> service.emitirBoleta(new DocumentoDto.EmitirBoleta(1L)))
                .satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
    }

    // ---------- factura ----------

    private DocumentoDto.EmitirFactura facturaRequest(String rut) {
        return new DocumentoDto.EmitirFactura(1L, rut, "  Ciclos del Sur SpA ", " Venta de bicicletas ", " Av. Colón 1234 ");
    }

    @Test
    @DisplayName("Factura: guarda los datos de la empresa (RUT normalizado y textos sin espacios)")
    void factura_ok() {
        pedidoPagado(11900, 0, linea(100, 1, 11900));
        when(datosFacturaRepository.save(any(DatosFactura.class))).thenAnswer(inv -> inv.getArgument(0));

        DocumentoDto.Detalle detalle = service.emitirFactura(facturaRequest("76543210-3"));

        assertThat(detalle.documento().tipo()).isEqualTo(DocumentoVenta.Tipo.FACTURA);
        assertThat(detalle.datosFactura().rutEmpresa()).isEqualTo("76543210-3");
        assertThat(detalle.datosFactura().razonSocial()).isEqualTo("Ciclos del Sur SpA");
        assertThat(detalle.datosFactura().giro()).isEqualTo("Venta de bicicletas");
        assertThat(detalle.datosFactura().direccionEmpresa()).isEqualTo("Av. Colón 1234");

        ArgumentCaptor<DatosFactura> captor = ArgumentCaptor.forClass(DatosFactura.class);
        verify(datosFacturaRepository).save(captor.capture());
        assertThat(captor.getValue().getIdDocumento()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Factura: 400 si el RUT de la empresa tiene un dígito verificador incorrecto")
    void factura_rutInvalido() {
        assertThatThrownBy(() -> service.emitirFactura(facturaRequest("76543210-9")))
                .satisfies(ex -> assertEstado(ex, HttpStatus.BAD_REQUEST))
                .hasMessageContaining("RUT");
        verify(pedidosClient, never()).obtenerPedido(any());
        verify(documentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Factura: la numeración es independiente de la de las boletas")
    void factura_numeracionPropia() {
        when(documentoRepository.existsByIdPedido(1L)).thenReturn(false);
        when(pedidosClient.obtenerPedido(1L)).thenReturn(pedido("PAGADO", 5000, 0));
        when(pedidosClient.listarDetallesPorPedido(1L)).thenReturn(List.of(linea(100, 1, 5000)));
        when(documentoRepository.findTopByTipoOrderByNumeroDesc(DocumentoVenta.Tipo.FACTURA)).thenReturn(Optional.empty());
        when(datosFacturaRepository.save(any(DatosFactura.class))).thenAnswer(inv -> inv.getArgument(0));
        simularGuardado();

        assertThat(service.emitirFactura(facturaRequest("76543210-3")).documento().numero()).isEqualTo(1L);
        verify(documentoRepository, never()).findTopByTipoOrderByNumeroDesc(DocumentoVenta.Tipo.BOLETA);
    }

    // ---------- consultas ----------

    private DocumentoVenta documento() {
        return DocumentoVenta.builder().idDocumento(1L).numero(3L).tipo(DocumentoVenta.Tipo.FACTURA).idPedido(1L)
                .rutCliente("12345678-5").fechaEmision(LocalDateTime.of(2026, 10, 7, 12, 0)).neto(10000).iva(1900).total(11900)
                .build();
    }

    @Test
    @DisplayName("findById: arma el documento con sus líneas y los datos de la factura")
    void findById_ok() {
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento()));
        when(detalleRepository.findByIdDocumento(1L)).thenReturn(List.of(DetalleDocumento.builder()
                .idDocumento(1L).idVariante(100L).descripcion("Variante 100").cantidad(1)
                .precioUnitario(11900).subtotal(11900).build()));
        when(datosFacturaRepository.findByIdDocumento(1L)).thenReturn(Optional.of(DatosFactura.builder()
                .idDocumento(1L).rutEmpresa("76543210-3").razonSocial("Ciclos").giro("Venta").direccionEmpresa("Calle").build()));

        DocumentoDto.Detalle detalle = service.findById(1L);

        assertThat(detalle.documento().numero()).isEqualTo(3L);
        assertThat(detalle.lineas()).hasSize(1);
        assertThat(detalle.datosFactura().rutEmpresa()).isEqualTo("76543210-3");
    }

    @Test
    @DisplayName("findById de una boleta: no trae datos de empresa")
    void findById_boletaSinEmpresa() {
        when(documentoRepository.findById(1L)).thenReturn(Optional.of(documento()));
        when(detalleRepository.findByIdDocumento(1L)).thenReturn(List.of());
        when(datosFacturaRepository.findByIdDocumento(1L)).thenReturn(Optional.empty());

        assertThat(service.findById(1L).datosFactura()).isNull();
    }

    @Test
    @DisplayName("findById, findByPedido y findByNumero: 404 si no existe")
    void consultas_noExiste() {
        when(documentoRepository.findById(9L)).thenReturn(Optional.empty());
        when(documentoRepository.findByIdPedido(9L)).thenReturn(Optional.empty());
        when(documentoRepository.findByTipoAndNumero(DocumentoVenta.Tipo.BOLETA, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(9L)).satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> service.findByPedido(9L)).satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
        assertThatThrownBy(() -> service.findByNumero(DocumentoVenta.Tipo.BOLETA, 9L))
                .satisfies(ex -> assertEstado(ex, HttpStatus.NOT_FOUND));
    }

    @Test
    @DisplayName("findByPedido y findByNumero: encuentran el documento")
    void consultas_ok() {
        when(documentoRepository.findByIdPedido(1L)).thenReturn(Optional.of(documento()));
        when(documentoRepository.findByTipoAndNumero(DocumentoVenta.Tipo.FACTURA, 3L)).thenReturn(Optional.of(documento()));
        when(detalleRepository.findByIdDocumento(1L)).thenReturn(List.of());
        when(datosFacturaRepository.findByIdDocumento(1L)).thenReturn(Optional.empty());

        assertThat(service.findByPedido(1L).documento().idDocumento()).isEqualTo(1L);
        assertThat(service.findByNumero(DocumentoVenta.Tipo.FACTURA, 3L).documento().numero()).isEqualTo(3L);
    }

    @Test
    @DisplayName("listarDocumentos: sin tipo trae todos y con tipo filtra; findByRutCliente lista los del cliente")
    void listados() {
        when(documentoRepository.findAll()).thenReturn(List.of(documento()));
        when(documentoRepository.findByTipo(DocumentoVenta.Tipo.BOLETA)).thenReturn(List.of());
        when(documentoRepository.findByRutCliente("12345678-5")).thenReturn(List.of(documento()));

        assertThat(service.listarDocumentos(null)).hasSize(1);
        assertThat(service.listarDocumentos(DocumentoVenta.Tipo.BOLETA)).isEmpty();
        assertThat(service.findByRutCliente("12345678-5")).extracting(DocumentoDto.Resumen::rutCliente).containsExactly("12345678-5");
    }
}
