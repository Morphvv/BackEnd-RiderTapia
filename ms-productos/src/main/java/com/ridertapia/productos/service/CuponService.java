package com.ridertapia.productos.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ridertapia.productos.model.Categoria;
import com.ridertapia.productos.model.Cupon;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.CuponRepository;

@Service
public class CuponService {

    @Autowired
    private CuponRepository cuponRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public Cupon guardarCupon(Cupon cupon) {
        cupon.setCategorias(resolverCategorias(cupon.getCategorias()));
        return cuponRepository.save(cupon);
    }

    public List<Cupon> listarCupones() {
        return cuponRepository.findAll();
    }

    public Optional<Cupon> findById(Long id) {
        return cuponRepository.findById(id);
    }

    public Cupon findByCodigo(String codigo) {
        return cuponRepository.findByCodigo(codigo).orElse(null);
    }

    public Cupon actualizarCupon(Long id, Cupon datos) {
        Cupon cuponActual = cuponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cupon no encontrado con id: " + id));
        cuponActual.setCodigo(datos.getCodigo());
        cuponActual.setTipoDescuento(datos.getTipoDescuento());
        cuponActual.setValor(datos.getValor());
        cuponActual.setFechaInicio(datos.getFechaInicio());
        cuponActual.setFechaTermino(datos.getFechaTermino());
        cuponActual.setLimiteUsos(datos.getLimiteUsos());
        cuponActual.setUsosActuales(datos.getUsosActuales());
        cuponActual.setActivo(datos.getActivo());
        cuponActual.setCategorias(resolverCategorias(datos.getCategorias()));
        return cuponRepository.save(cuponActual);
    }

    public void eliminarCupon(Long id) {
        cuponRepository.deleteById(id);
    }

    private List<Categoria> resolverCategorias(List<Categoria> lista) {
        if (lista == null || lista.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> ids = lista.stream().map(Categoria::getIdCategoria).toList();
        List<Categoria> encontrados = categoriaRepository.findAllById(ids);
        if (encontrados.size() != ids.size()) {
            throw new RuntimeException("Categoria no encontrada en: " + ids);
        }
        return new ArrayList<>(encontrados);
    }
}
