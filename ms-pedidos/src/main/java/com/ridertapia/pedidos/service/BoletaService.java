package com.ridertapia.pedidos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.pedidos.model.Boleta;
import com.ridertapia.pedidos.model.Pedido;
import com.ridertapia.pedidos.repository.BoletaRepository;
import com.ridertapia.pedidos.repository.PedidoRepository;

@Service
public class BoletaService {

    @Autowired
    private BoletaRepository boletaRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public Boleta guardarBoleta(Boleta boleta) {
        boleta.setPedido(resolverPedido(boleta.getPedido()));
        return boletaRepository.save(boleta);
    }

    public List<Boleta> listarBoletas() {
        return boletaRepository.findAll();
    }

    public Optional<Boleta> findById(Long id) {
        return boletaRepository.findById(id);
    }

    public List<Boleta> findByPedido(Long idPedido) {
        return boletaRepository.findByPedidoIdPedido(idPedido);
    }

    public Boleta actualizarBoleta(Long id, Boleta datos) {
        Boleta boletaActual = boletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Boleta no encontrada con id: " + id));
        boletaActual.setFolio(datos.getFolio());
        boletaActual.setFechaEmision(datos.getFechaEmision());
        boletaActual.setMontoNeto(datos.getMontoNeto());
        boletaActual.setIva(datos.getIva());
        boletaActual.setTotal(datos.getTotal());
        boletaActual.setPdfUrl(datos.getPdfUrl());
        boletaActual.setPedido(resolverPedido(datos.getPedido()));
        return boletaRepository.save(boletaActual);
    }

    public void eliminarBoleta(Long id) {
        boletaRepository.deleteById(id);
    }

    private Pedido resolverPedido(Pedido pedido) {
        if (pedido == null || pedido.getIdPedido() == null) {
            throw new RuntimeException("Pedido no encontrado con id: null");
        }
        return pedidoRepository.findById(pedido.getIdPedido())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + pedido.getIdPedido()));
    }
}
