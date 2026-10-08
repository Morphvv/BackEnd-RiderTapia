package cl.ridertapia.reportesservice.service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class CriterioVenta {

    private final Set<String> estadosVenta;

    public CriterioVenta(@Value("${reportes.estados-venta}") String estadosConfigurados) {
        this.estadosVenta = Arrays.stream(estadosConfigurados.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(estado -> !estado.isEmpty())
                .collect(Collectors.toSet());
    }

    public boolean esVenta(String estadoPedido) {
        return estadoPedido != null && estadosVenta.contains(estadoPedido.trim().toUpperCase());
    }

    public Set<String> estados() {
        return estadosVenta;
    }
}
