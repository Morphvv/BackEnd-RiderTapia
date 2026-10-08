package cl.ridertapia.soporteservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI soporteOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de Soporte y Postventa - Rider Tapia Biker")
                .version("1.0.0")
                .description("Reseñas, garantías, devoluciones, mensajes de contacto y seguimiento posterior a la compra. "
                        + "Los RUT se validan contra usuario-service."));
    }
}
