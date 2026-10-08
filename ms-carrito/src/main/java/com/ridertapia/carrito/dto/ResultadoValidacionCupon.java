package com.ridertapia.carrito.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Resultado de validar un cupón contra el carrito (HU-22). No es tabla: se calcula al momento.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoValidacionCupon {

    private Boolean valido;

    private String mensaje;

    private Integer montoDescuento;
}
