package br.com.fiap.zelo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ZeloApplicationTests {

    @Test
    void contextoSobeComTodasAsMigrations() {
        // Falha se qualquer bean, migration do Flyway ou mapeamento JPA estiver inconsistente.
    }
}
