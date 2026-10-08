package com.ridertapia.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = org.springdoc.core.configuration.SpringDocHateoasConfiguration.class)
public class ProductoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductoApplication.class, args);
    }
}
