package cl.ridertapia.soporteservice.util;

public final class RutUtil {

    /** Formato esperado: 12345678-9 (con guion, K permitida). */
    public static final String RUT_REGEX = "^\\d{7,8}-[\\dkK]$";

    private RutUtil() {
    }

    /** Deja el RUT sin espacios y en mayúsculas para guardarlo siempre igual. */
    public static String normalizar(String rut) {
        return rut == null ? null : rut.trim().toUpperCase();
    }
}
