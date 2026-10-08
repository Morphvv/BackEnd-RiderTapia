package com.ridertapia.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

@SpringBootTest
class GatewayApplicationTests {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void contextLoads() {
        assertNotNull(routeLocator);
    }

    @Test
    void cargaUnaRutaPorMicroservicio() {
        List<String> ids = routeLocator.getRoutes().map(Route::getId).collectList().block();

        assertNotNull(ids);
        assertEquals(List.of("ms-usuario", "ms-cliente", "ms-soporte", "ms-reportes", "ms-productos",
                "ms-pedidos", "ms-pagos", "ms-inventario", "ms-carrito"), ids);
    }
}
