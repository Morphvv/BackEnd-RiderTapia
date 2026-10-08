package cl.ridertapia.reportesservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reportesOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de Reportes - Rider Tapia Biker")
                .version("1.0.0")
                .description("Reportes de ventas (diarios, semanales y mensuales), productos más vendidos "
                        + "y emisión de boletas y facturas. Los datos de ventas se obtienen del servicio de pedidos."));
    }
}
