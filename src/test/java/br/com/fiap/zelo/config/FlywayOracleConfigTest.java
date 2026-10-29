package br.com.fiap.zelo.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Simula o script do time de BD recriando as tabelas depois que o Java já rodou. */
class FlywayOracleConfigTest {

    private static final String HISTORICO = "flyway_schema_history";

    private DriverManagerDataSource dataSource;
    private JdbcTemplate jdbc;

    @BeforeEach
    void criarBanco() {
        dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=Oracle;DB_CLOSE_DELAY=-1", "sa", "");
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE T_CH_CLINICA (id_clinica NUMBER PRIMARY KEY)");
        jdbc.execute("CREATE TABLE T_CH_CLINICA_EQUIPE (id_clinica NUMBER, id_usuario NUMBER)");
        jdbc.execute("CREATE TABLE \"" + HISTORICO + "\" (versao VARCHAR2(10))");
    }

    @Test
    void descartaSoOsObjetosDoJavaQuandoAsTabelasForamRecriadas() {
        assertThat(FlywayOracleConfig.sincronizarComScriptDoBanco(dataSource, HISTORICO)).isTrue();

        assertThat(existe("T_CH_CLINICA_EQUIPE")).isFalse();
        assertThat(existe(HISTORICO)).isFalse();
        assertThat(existe("T_CH_CLINICA")).isTrue();
    }

    @Test
    void naoMexeEmNadaQuandoAsMigrationsDoJavaEstaoAplicadas() {
        jdbc.execute("ALTER TABLE T_CH_CLINICA ADD cd_convite VARCHAR2(12)");

        assertThat(FlywayOracleConfig.sincronizarComScriptDoBanco(dataSource, HISTORICO)).isFalse();

        assertThat(existe("T_CH_CLINICA_EQUIPE")).isTrue();
        assertThat(existe(HISTORICO)).isTrue();
    }

    @Test
    void naoMexeEmNadaNaPrimeiraConexao() {
        jdbc.execute("DROP TABLE \"" + HISTORICO + "\"");

        assertThat(FlywayOracleConfig.sincronizarComScriptDoBanco(dataSource, HISTORICO)).isFalse();

        assertThat(existe("T_CH_CLINICA_EQUIPE")).isTrue();
    }

    private boolean existe(String tabela) {
        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?", Integer.class, tabela);
        return total != null && total > 0;
    }
}
