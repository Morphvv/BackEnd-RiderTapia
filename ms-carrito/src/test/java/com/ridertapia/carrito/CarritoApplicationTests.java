package com.ridertapia.carrito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CarritoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainClassNoEsNull() {
        assertNotNull(new CarritoApplication());
    }

    @Test
    void restTemplateBeanNoEsNull() {
        assertNotNull(new CarritoApplication().restTemplate());
    }
}
