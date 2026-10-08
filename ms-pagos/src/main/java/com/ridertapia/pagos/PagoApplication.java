package com.ridertapia.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication(exclude = org.springdoc.core.configuration.SpringDocHateoasConfiguration.class)
public class PagoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagoApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
