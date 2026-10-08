package cl.ridertapia.reportesservice.client;

import cl.ridertapia.reportesservice.exception.ApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Cliente HTTP hacia el servicio de pedidos.
 * Ese servicio responde con Spring HATEOAS: las listas llegan dentro de "_embedded" y cada
 * elemento trae "_links". Por eso las listas se leen como JSON genérico y se extraen a mano.
 */
@Component
@RequiredArgsConstructor
public class PedidosClient {

    private final RestClient pedidosRestClient;
    private final ObjectMapper objectMapper;

    /** Todos los pedidos (el servicio de pedidos no tiene filtro por fecha, se filtra aquí). */
    public List<PedidoDto> listarPedidos() {
        return extraerLista(consultar("/api/v1/pedidos"), PedidoDto.class);
    }

    /** Todos los detalles de todos los pedidos. */
    public List<DetallePedidoDto> listarDetalles() {
        return extraerLista(consultar("/api/v1/detalles"), DetallePedidoDto.class);
    }

    public List<DetallePedidoDto> listarDetallesPorPedido(Long idPedido) {
        return extraerLista(consultar("/api/v1/detalles/pedido/" + idPedido), DetallePedidoDto.class);
    }

    public PedidoDto obtenerPedido(Long idPedido) {
        try {
            PedidoDto pedido = pedidosRestClient.get()
                    .uri("/api/v1/pedidos/{id}", idPedido)
                    .retrieve()
                    .body(PedidoDto.class);
            if (pedido == null) {
                throw ApiException.noEncontrado("No existe el pedido " + idPedido);
            }
            return pedido;
        } catch (HttpClientErrorException.NotFound e) {
            throw ApiException.noEncontrado("No existe el pedido " + idPedido);
        } catch (RestClientException e) {
            throw ApiException.servicioNoDisponible("No se pudo consultar el servicio de pedidos");
        }
    }

    private JsonNode consultar(String ruta) {
        try {
            return pedidosRestClient.get().uri(ruta).retrieve().body(JsonNode.class);
        } catch (RestClientException e) {
            throw ApiException.servicioNoDisponible("No se pudo consultar el servicio de pedidos");
        }
    }

    /** Acepta un arreglo JSON simple o una respuesta HAL ("_embedded": { "xxxList": [ ... ] }). */
    private <T> List<T> extraerLista(JsonNode raiz, Class<T> tipo) {
        JsonNode arreglo = null;
        if (raiz != null) {
            if (raiz.isArray()) {
                arreglo = raiz;
            } else if (raiz.has("_embedded")) {
                Iterator<JsonNode> coleccionesEmbebidas = raiz.get("_embedded").elements();
                if (coleccionesEmbebidas.hasNext()) {
                    arreglo = coleccionesEmbebidas.next();
                }
            }
        }

        List<T> resultado = new ArrayList<>();
        // HAL omite "_embedded" cuando la lista está vacía, por eso una lista vacía es válida
        if (arreglo != null && arreglo.isArray()) {
            for (JsonNode elemento : arreglo) {
                resultado.add(objectMapper.convertValue(elemento, tipo));
            }
        }
        return resultado;
    }
}
