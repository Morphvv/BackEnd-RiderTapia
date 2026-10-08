package com.ridertapia.pedidos.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.pedidos.model.DatosVisitante;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.repository.DatosVisitanteRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DatosVisitanteRepository datosVisitanteRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.clientes.url}")
    private String clientesUrl;

    public Pedido guardarPedido(Pedido pedido) {
        validarCliente(pedido.getRutCliente());
        pedido.setDatosVisitante(resolverDatosVisitante(pedido.getDatosVisitante()));
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido findByNumero(String numero) {
        return pedidoRepository.findByNumero(numero).orElse(null);
    }

    public Pedido actualizarPedido(Long id, Pedido datos) {
        Pedido pedidoActual = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + id));
        pedidoActual.setNumero(datos.getNumero());
        pedidoActual.setFechaCreacion(datos.getFechaCreacion());
        pedidoActual.setFechaExpiracion(datos.getFechaExpiracion());
        pedidoActual.setEstado(datos.getEstado());
        pedidoActual.setCanal(datos.getCanal());
        pedidoActual.setSubtotal(datos.getSubtotal());
        pedidoActual.setDescuento(datos.getDescuento());
        pedidoActual.setTotal(datos.getTotal());
        pedidoActual.setMotivoAnulacion(datos.getMotivoAnulacion());
        pedidoActual.setRutCliente(datos.getRutCliente());
        pedidoActual.setIdCupon(datos.getIdCupon());
        pedidoActual.setIdEmpleado(datos.getIdEmpleado());
        pedidoActual.setDatosVisitante(resolverDatosVisitante(datos.getDatosVisitante()));
        return pedidoRepository.save(pedidoActual);
    }

    public void eliminarPedido(Long id) {
        pedidoRepository.deleteById(id);
    }

    // Consulta a ms-cliente: GET /api/v1/clientes/{rut}. Los pedidos de visitantes (sin RUT) no se validan
    private void validarCliente(String rutCliente) {
        if (rutCliente == null) {
            return;
        }
        try {
            restTemplate.getForEntity(URI.create(clientesUrl + "/" + rutCliente), String.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Cliente no encontrado con rut: " + rutCliente);
        }
    }

    private DatosVisitante resolverDatosVisitante(DatosVisitante datosVisitante) {
        if (datosVisitante == null || datosVisitante.getIdDatosVisitante() == null) {
            return null;
        }
        return datosVisitanteRepository.findById(datosVisitante.getIdDatosVisitante())
                .orElseThrow(() -> new RuntimeException("DatosVisitante no encontrado con id: " + datosVisitante.getIdDatosVisitante()));
    }
}
