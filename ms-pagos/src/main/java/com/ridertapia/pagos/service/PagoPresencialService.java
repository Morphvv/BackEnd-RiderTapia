package com.ridertapia.pagos.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.pagos.model.PagoPresencial;
import com.ridertapia.pagos.repository.PagoPresencialRepository;

@Service
public class PagoPresencialService {

    @Autowired
    private PagoPresencialRepository pagoPresencialRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.pedidos.url}")
    private String pedidoUrl;

    public PagoPresencial guardarPagoPresencial(PagoPresencial pagoPresencial) {
        if (!existePedido(pagoPresencial.getIdPedido())) {
            throw new RuntimeException("Pedido no encontrado con id: " + pagoPresencial.getIdPedido());
        }
        return pagoPresencialRepository.save(pagoPresencial);
    }

    public List<PagoPresencial> listarPagosPresenciales() {
        return pagoPresencialRepository.findAll();
    }

    public Optional<PagoPresencial> findById(Long id) {
        return pagoPresencialRepository.findById(id);
    }

    public List<PagoPresencial> findByIdPedido(Long idPedido) {
        return pagoPresencialRepository.findByIdPedido(idPedido);
    }

    public PagoPresencial actualizarPagoPresencial(Long id, PagoPresencial datos) {
        PagoPresencial pagoPresencialActual = pagoPresencialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PagoPresencial no encontrado con id: " + id));
        pagoPresencialActual.setIdPedido(datos.getIdPedido());
        pagoPresencialActual.setMonto(datos.getMonto());
        pagoPresencialActual.setFecha(datos.getFecha());
        pagoPresencialActual.setEstado(datos.getEstado());
        pagoPresencialActual.setMedioPago(datos.getMedioPago());
        return pagoPresencialRepository.save(pagoPresencialActual);
    }

    public void eliminarPagoPresencial(Long id) {
        pagoPresencialRepository.deleteById(id);
    }

    // Consulta a ms-pedidos: GET /api/v1/pedidos/{id}
    private boolean existePedido(Long id) {
        try {
            restTemplate.getForEntity(URI.create(pedidoUrl + "/" + id), String.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
