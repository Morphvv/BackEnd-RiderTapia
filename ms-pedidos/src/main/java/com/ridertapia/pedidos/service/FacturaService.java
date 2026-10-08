package com.ridertapia.pedidos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.pedidos.model.Factura;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.repository.FacturaRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public Factura guardarFactura(Factura factura) {
        factura.setPedido(resolverPedido(factura.getPedido()));
        return facturaRepository.save(factura);
    }

    public List<Factura> listarFacturas() {
        return facturaRepository.findAll();
    }

    public Optional<Factura> findById(Long id) {
        return facturaRepository.findById(id);
    }

    public List<Factura> findByPedido(Long idPedido) {
        return facturaRepository.findByPedidoIdPedido(idPedido);
    }

    public Factura actualizarFactura(Long id, Factura datos) {
        Factura facturaActual = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con id: " + id));
        facturaActual.setFolio(datos.getFolio());
        facturaActual.setFechaEmision(datos.getFechaEmision());
        facturaActual.setMontoNeto(datos.getMontoNeto());
        facturaActual.setIva(datos.getIva());
        facturaActual.setTotal(datos.getTotal());
        facturaActual.setPdfUrl(datos.getPdfUrl());
        facturaActual.setPedido(resolverPedido(datos.getPedido()));
        facturaActual.setRutEmpresa(datos.getRutEmpresa());
        facturaActual.setRazonSocial(datos.getRazonSocial());
        facturaActual.setGiro(datos.getGiro());
        facturaActual.setDireccionEmpresa(datos.getDireccionEmpresa());
        return facturaRepository.save(facturaActual);
    }

    public void eliminarFactura(Long id) {
        facturaRepository.deleteById(id);
    }

    private Pedido resolverPedido(Pedido pedido) {
        if (pedido == null || pedido.getIdPedido() == null) {
            throw new RuntimeException("Pedido no encontrado con id: null");
        }
        return pedidoRepository.findById(pedido.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + pedido.getIdPedido()));
    }
}
