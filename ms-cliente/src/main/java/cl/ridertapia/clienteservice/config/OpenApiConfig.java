package cl.ridertapia.clienteservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clienteOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de Clientes - Rider Tapia Biker")
                .version("1.0.0")
                .description("Perfil personal y datos de despacho de los clientes. "
                        + "El identificador es el RUT de la cuenta, que debe existir en usuario-service con rol CLIENTE."));
    }
}
