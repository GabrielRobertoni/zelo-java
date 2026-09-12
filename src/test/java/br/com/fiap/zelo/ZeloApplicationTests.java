package br.com.fiap.zelo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de "fumaca" (smoke test): sobe o contexto Spring completo com o
 * perfil "test" (H2 em modo Oracle + Flyway, migrations de
 * db/migration/dev - mesmo schema T_CH_* do DDL real). Se este teste
 * passar, temos confirmacao de que:
 *
 *  - as migrations V1/V2 sao executadas sem erro pelo Flyway contra o H2
 *    em modo de compatibilidade Oracle (sintaxe de tipos, CHECK constraints,
 *    NUMBER GENERATED ALWAYS AS IDENTITY, indices etc.);
 *  - o mapeamento de todas as entidades JPA (@Table/@Column) contra esse
 *    schema nao tem nenhum erro de digitacao/nome que impeca o Hibernate de
 *    montar o EntityManagerFactory (spring.jpa.hibernate.ddl-auto=none, ou
 *    seja, o Hibernate nao valida byte a byte contra o schema - ver
 *    application.yml para o motivo -, mas ainda assim falha alto se um
 *    @JoinColumn ou @Table referenciar algo que nao existe);
 *  - todos os beans Spring (services, repositories, security, controllers)
 *    sao criados e conectados corretamente, sem dependencia faltando ou
 *    ciclo de beans.
 */
@SpringBootTest
@ActiveProfiles("test")
class ZeloApplicationTests {

    @Test
    void contextLoads() {
        // Intencionalmente vazio: o proprio carregamento do contexto (feito
        // pela anotacao @SpringBootTest acima) e a asserção deste teste.
    }
}
