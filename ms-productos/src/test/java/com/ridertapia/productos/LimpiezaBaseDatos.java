package com.ridertapia.productos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ridertapia.productos.repository.AvisoStockRepository;
import com.ridertapia.productos.repository.CategoriaRepository;
import com.ridertapia.productos.repository.CompatibilidadRepository;
import com.ridertapia.productos.repository.CuponRepository;
import com.ridertapia.productos.repository.EspecificacionTecnicaRepository;
import com.ridertapia.productos.repository.ImagenProductoRepository;
import com.ridertapia.productos.repository.MarcaRepository;
import com.ridertapia.productos.repository.OfertaRepository;
import com.ridertapia.productos.repository.ProductoRepository;
import com.ridertapia.productos.repository.ResenaRepository;
import com.ridertapia.productos.repository.VarianteProductoRepository;

/**
 * Deja la base H2 vacía antes de cada test de integración.
 * Borra en orden (primero los registros que dependen de otros) para no romper las claves foráneas.
 */
@Component
public class LimpiezaBaseDatos {

    @Autowired
    private AvisoStockRepository avisoStockRepository;

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private OfertaRepository ofertaRepository;

    @Autowired
    private EspecificacionTecnicaRepository especificacionTecnicaRepository;

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    @Autowired
    private VarianteProductoRepository varianteProductoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CompatibilidadRepository compatibilidadRepository;

    @Autowired
    private CuponRepository cuponRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    public void limpiar() {
        avisoStockRepository.deleteAll();
        resenaRepository.deleteAll();
        ofertaRepository.deleteAll();
        especificacionTecnicaRepository.deleteAll();
        imagenProductoRepository.deleteAll();
        varianteProductoRepository.deleteAll();
        productoRepository.deleteAll();
        compatibilidadRepository.deleteAll();
        cuponRepository.deleteAll();
        categoriaRepository.deleteAll(categoriaRepository.findAll().stream()
                .filter(x -> x.getCategoriaPadre() != null).toList());
        categoriaRepository.deleteAll();
        marcaRepository.deleteAll();
    }
}
