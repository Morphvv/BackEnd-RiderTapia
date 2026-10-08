package cl.ridertapia.usuarioservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI usuarioOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API de Usuarios - Rider Tapia Biker")
                .version("1.0.0")
                .description("Registro, consulta, actualización, inicio de sesión y "
                        + "recuperación de contraseña de las cuentas del sistema. "
                        + "El identificador de cada usuario es su RUT (formato 12345678-9)."));
    }
}
