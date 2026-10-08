package cl.ridertapia.clienteservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public static ApiException noEncontrado(String mensaje) {
        return new ApiException(HttpStatus.NOT_FOUND, mensaje);
    }

    public static ApiException conflicto(String mensaje) {
        return new ApiException(HttpStatus.CONFLICT, mensaje);
    }

    public static ApiException solicitudInvalida(String mensaje) {
        return new ApiException(HttpStatus.BAD_REQUEST, mensaje);
    }

    public static ApiException servicioNoDisponible(String mensaje) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE, mensaje);
    }
}
