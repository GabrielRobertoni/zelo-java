package br.com.fiap.zelo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Convivência com o script do time de Banco de Dados (perfil oracle).
 *
 * <p>O script deles recria as tabelas T_CH_* do zero (DROP + CREATE), mas não conhece o que só o
 * Java cria: a tabela T_CH_CLINICA_EQUIPE, a coluna cd_convite e o histórico do Flyway. Se o script
 * for executado de novo depois do Java, o histórico diria que tudo já foi aplicado e a aplicação não
 * subiria. Antes de migrar, este bean detecta esse caso (histórico presente, mas T_CH_CLINICA sem
 * cd_convite) e descarta só os objetos do Java, para o Flyway reaplicá-los. Nenhuma tabela, package
 * ou trigger do time de BD é alterado.
 *
 * <p>O {@code repair} realinha o histórico quando uma migration já aplicada teve o texto ajustado
 * (ex.: comentários): as migrations do Oracle são idempotentes, então isso não duplica dados.
 */
@Configuration
@Profile("oracle")
public class FlywayOracleConfig {

    private static final Logger LOG = LoggerFactory.getLogger(FlywayOracleConfig.class);
    private static final String TABELA_EQUIPE = "T_CH_CLINICA_EQUIPE";

    @Bean
    public FlywayMigrationStrategy migracaoCompativelComScriptDoBanco() {
        return flyway -> {
            sincronizarComScriptDoBanco(flyway.getConfiguration().getDataSource(),
                    flyway.getConfiguration().getTable());
            flyway.repair();
            flyway.migrate();
        };
    }

    /** Devolve {@code true} se as tabelas foram recriadas pelo time de BD e os objetos do Java foram descartados. */
    static boolean sincronizarComScriptDoBanco(DataSource dataSource, String tabelaHistorico) {
        try (Connection conexao = dataSource.getConnection()) {
            DatabaseMetaData meta = conexao.getMetaData();
            String schema = conexao.getSchema();
            boolean tabelasRecriadas = existeTabela(meta, schema, tabelaHistorico)
                    && existeTabela(meta, schema, "T_CH_CLINICA")
                    && !existeColuna(meta, schema, "T_CH_CLINICA", "CD_CONVITE");
            if (!tabelasRecriadas) {
                return false;
            }
            LOG.warn("As tabelas T_CH_* foram recriadas pelo script do time de BD. Reaplicando as migrations do Java.");
            try (Statement sql = conexao.createStatement()) {
                if (existeTabela(meta, schema, TABELA_EQUIPE)) {
                    sql.execute("DROP TABLE " + TABELA_EQUIPE);
                }
                sql.execute("DROP TABLE \"" + tabelaHistorico + "\"");
            }
            return true;
        } catch (SQLException e) {
            throw new IllegalStateException("Não foi possível verificar o schema antes das migrations.", e);
        }
    }

    private static boolean existeTabela(DatabaseMetaData meta, String schema, String tabela) throws SQLException {
        try (ResultSet rs = meta.getTables(null, schema, tabela, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static boolean existeColuna(DatabaseMetaData meta, String schema, String tabela, String coluna)
            throws SQLException {
        try (ResultSet rs = meta.getColumns(null, schema, tabela, coluna)) {
            return rs.next();
        }
    }
}
