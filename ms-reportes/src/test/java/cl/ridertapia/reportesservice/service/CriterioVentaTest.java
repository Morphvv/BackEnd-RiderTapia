package cl.ridertapia.reportesservice.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CriterioVentaTest {

    @Test
    @DisplayName("Reconoce los estados configurados sin importar mayúsculas ni espacios")
    void esVenta() {
        CriterioVenta criterio = new CriterioVenta(" pagado , Retirado ,, LISTO_PARA_RETIRO ");

        assertThat(criterio.esVenta("PAGADO")).isTrue();
        assertThat(criterio.esVenta("retirado")).isTrue();
        assertThat(criterio.esVenta("  Listo_Para_Retiro ")).isTrue();
        assertThat(criterio.estados()).containsExactlyInAnyOrder("PAGADO", "RETIRADO", "LISTO_PARA_RETIRO");
    }

    @Test
    @DisplayName("Los estados no configurados y el nulo no cuentan como venta")
    void noEsVenta() {
        CriterioVenta criterio = new CriterioVenta("PAGADO");

        assertThat(criterio.esVenta("PENDIENTE_PAGO")).isFalse();
        assertThat(criterio.esVenta("")).isFalse();
        assertThat(criterio.esVenta(null)).isFalse();
    }
}
