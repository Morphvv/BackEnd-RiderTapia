package com.ridertapia.carrito.dto;

import com.ridertapia.carrito.model.TipoAviso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Aviso sobre un ítem del carrito al revalidarlo antes de pagar (HU-26). No es tabla: se calcula al momento.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvisoCambio {

    private TipoAviso tipo;

    private String mensaje;

    private Integer precioAnterior;

    private Integer precioNuevo;

    private Integer stockDisponible;

    private Long idItemCarrito;
}
