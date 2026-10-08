package com.ridertapia.pagos.service;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ridertapia.pagos.model.PagoWebPay;
import com.ridertapia.pagos.repository.PagoWebPayRepository;

@Service
public class PagoWebPayService {

    @Autowired
    private PagoWebPayRepository pagoWebPayRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ms.pedidos.url}")
    private String pedidoUrl;

    public PagoWebPay guardarPagoWebPay(PagoWebPay pagoWebPay) {
        if (!existePedido(pagoWebPay.getIdPedido())) {
            throw new RuntimeException("Pedido no encontrado con id: " + pagoWebPay.getIdPedido());
        }
        return pagoWebPayRepository.save(pagoWebPay);
    }

    public List<PagoWebPay> listarPagosWebPay() {
        return pagoWebPayRepository.findAll();
    }

    public Optional<PagoWebPay> findById(Long id) {
        return pagoWebPayRepository.findById(id);
    }

    public List<PagoWebPay> findByIdPedido(Long idPedido) {
        return pagoWebPayRepository.findByIdPedido(idPedido);
    }

    public PagoWebPay actualizarPagoWebPay(Long id, PagoWebPay datos) {
        PagoWebPay pagoWebPayActual = pagoWebPayRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PagoWebPay no encontrado con id: " + id));
        pagoWebPayActual.setIdPedido(datos.getIdPedido());
        pagoWebPayActual.setMonto(datos.getMonto());
        pagoWebPayActual.setFecha(datos.getFecha());
        pagoWebPayActual.setEstado(datos.getEstado());
        pagoWebPayActual.setToken(datos.getToken());
        pagoWebPayActual.setCodigoAutorizacion(datos.getCodigoAutorizacion());
        pagoWebPayActual.setUltimosDigitosTarjeta(datos.getUltimosDigitosTarjeta());
        return pagoWebPayRepository.save(pagoWebPayActual);
    }

    public void eliminarPagoWebPay(Long id) {
        pagoWebPayRepository.deleteById(id);
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
