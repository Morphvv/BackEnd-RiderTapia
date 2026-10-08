package cl.ridertapia.usuarioservice.util;

public final class RutUtil {

    private RutUtil() {
    }

    /** Valida el dígito verificador de un RUT con formato 12345678-9 (módulo 11). */
    public static boolean esValido(String rut) {
        if (rut == null || !rut.matches("^\\d{7,8}-[\\dkK]$")) {
            return false;
        }
        String[] partes = rut.split("-");
        String cuerpo = partes[0];
        char dvIngresado = Character.toUpperCase(partes[1].charAt(0));

        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        char dvEsperado = (resto == 11) ? '0' : (resto == 10) ? 'K' : (char) ('0' + resto);
        return dvIngresado == dvEsperado;
    }

    /** Deja el RUT en mayúsculas para guardarlo siempre igual (K final). */
    public static String normalizar(String rut) {
        return rut == null ? null : rut.trim().toUpperCase();
    }
}
