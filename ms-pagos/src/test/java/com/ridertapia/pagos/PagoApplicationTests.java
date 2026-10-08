package com.ridertapia.pagos;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PagoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainClassNoEsNull() {
        assertNotNull(new PagoApplication());
    }

    @Test
    void restTemplateBeanNoEsNull() {
        assertNotNull(new PagoApplication().restTemplate());
    }
}
