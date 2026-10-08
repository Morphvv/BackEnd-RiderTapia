package com.ridertapia.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que muestra el mini carrito del encabezado (HU-25). No es tabla: se calcula al momento.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumenCarrito {

    private Integer cantidadItems;

    private Integer total;
}
