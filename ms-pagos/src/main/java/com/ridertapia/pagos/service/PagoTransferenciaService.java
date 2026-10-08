package com.ridertapia.pagos.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.pagos.model.PagoTransferencia;
import com.ridertapia.pagos.repository.PagoTransferenciaRepository;

@Service
public class PagoTransferenciaService {

    @Autowired
    private PagoTransferenciaRepository pagoTransferenciaRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.pedidos.url}")
    private String pedidoUrl;

    public PagoTransferencia guardarPagoTransferencia(PagoTransferencia pagoTransferencia) {
        if (!existePedido(pagoTransferencia.getIdPedido())) {
            throw new RuntimeException("Pedido no encontrado con id: " + pagoTransferencia.getIdPedido());
        }
        return pagoTransferenciaRepository.save(pagoTransferencia);
    }

    public List<PagoTransferencia> listarPagosTransferencia() {
        return pagoTransferenciaRepository.findAll();
    }

    public Optional<PagoTransferencia> findById(Long id) {
        return pagoTransferenciaRepository.findById(id);
    }

    public List<PagoTransferencia> findByIdPedido(Long idPedido) {
        return pagoTransferenciaRepository.findByIdPedido(idPedido);
    }

    public PagoTransferencia actualizarPagoTransferencia(Long id, PagoTransferencia datos) {
        PagoTransferencia pagoTransferenciaActual = pagoTransferenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PagoTransferencia no encontrado con id: " + id));
        pagoTransferenciaActual.setIdPedido(datos.getIdPedido());
        pagoTransferenciaActual.setMonto(datos.getMonto());
        pagoTransferenciaActual.setFecha(datos.getFecha());
        pagoTransferenciaActual.setEstado(datos.getEstado());
        pagoTransferenciaActual.setComprobanteUrl(datos.getComprobanteUrl());
        pagoTransferenciaActual.setFechaLimite(datos.getFechaLimite());
        pagoTransferenciaActual.setObservacion(datos.getObservacion());
        return pagoTransferenciaRepository.save(pagoTransferenciaActual);
    }

    public void eliminarPagoTransferencia(Long id) {
        pagoTransferenciaRepository.deleteById(id);
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
