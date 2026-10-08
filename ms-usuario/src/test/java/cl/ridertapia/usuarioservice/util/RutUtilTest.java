package cl.ridertapia.usuarioservice.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RutUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"12345678-5", "11111111-1", "12342313-5", "10000013-K", "10000013-k"})
    @DisplayName("Acepta RUT con dígito verificador correcto")
    void esValido_rutCorrecto(String rut) {
        assertThat(RutUtil.esValido(rut)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678-9", "12345678", "1234-5", "abcdefgh-1", "12.345.678-5", "123456789-5"})
    @DisplayName("Rechaza RUT con dígito incorrecto o formato inválido")
    void esValido_rutIncorrecto(String rut) {
        assertThat(RutUtil.esValido(rut)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Rechaza RUT nulo o vacío")
    void esValido_nuloOVacio(String rut) {
        assertThat(RutUtil.esValido(rut)).isFalse();
    }

    @Test
    @DisplayName("normalizar deja el RUT en mayúsculas y sin espacios")
    void normalizar() {
        assertThat(RutUtil.normalizar("  10000013-k ")).isEqualTo("10000013-K");
        assertThat(RutUtil.normalizar(null)).isNull();
    }
}
