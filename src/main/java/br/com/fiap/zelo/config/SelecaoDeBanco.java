package br.com.fiap.zelo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * Escolhe o banco antes de a aplicação subir.
 *
 * <p>Se o Oracle do grupo responder e já tiver as tabelas do time de BD, a aplicação liga o perfil
 * {@code oracle} sozinha. Se não responder (sem rede, FIAP fora do ar, tabelas ausentes), segue com o
 * H2 embutido. E, se a aplicação falhar ao iniciar com o Oracle (ex.: erro de migration), ela sobe de
 * novo com o H2: nunca deixa de abrir por causa do banco.
 *
 * <p>Os dados de conexão podem ser trocados pelas variáveis ORACLE_URL, ORACLE_USER e ORACLE_PASSWORD.
 * ZELO_ORACLE=false força o H2; um perfil escolhido explicitamente (spring.profiles.active) é respeitado.
 */
public final class SelecaoDeBanco {

    private static final Logger LOG = LoggerFactory.getLogger(SelecaoDeBanco.class);

    static final String URL_PADRAO = "jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL";
    static final String USUARIO_PADRAO = "rm562380";
    static final String SENHA_PADRAO = "040207";
    private static final String TEMPO_LIMITE_MS = "8000";

    private SelecaoDeBanco() {
    }

    record ConexaoOracle(String url, String usuario, String senha) {

        static ConexaoOracle de(Map<String, String> ambiente) {
            return new ConexaoOracle(ambiente.getOrDefault("ORACLE_URL", URL_PADRAO),
                    ambiente.getOrDefault("ORACLE_USER", USUARIO_PADRAO),
                    ambiente.getOrDefault("ORACLE_PASSWORD", SENHA_PADRAO));
        }

        /** Propriedades lidas pelo perfil oracle do application.yml. */
        Map<String, Object> comoPropriedades() {
            return Map.of("zelo.oracle.url", url, "zelo.oracle.usuario", usuario, "zelo.oracle.senha", senha);
        }
    }

    /** Sobe a aplicação no Oracle quando possível; se ele falhar em qualquer etapa, sobe de novo com o H2. */
    public static void iniciar(Class<?> fonte, String[] args) {
        Map<String, String> ambiente = System.getenv();
        ConexaoOracle conexao = ConexaoOracle.de(ambiente);
        boolean usarOracle = deveTentarOracle(ambiente, System.getProperties(), args) && oracleDisponivel(conexao);
        if (!usarOracle) {
            criar(fonte, conexao, false).run(args);
            return;
        }
        LOG.info("Oracle disponível em {} (usuário {}): usando o banco do grupo.", conexao.url(), conexao.usuario());
        try {
            criar(fonte, conexao, true).run(args);
        } catch (RuntimeException e) {
            LOG.error("A aplicação não conseguiu iniciar com o Oracle ({}). Reiniciando com o H2 embutido.",
                    causaRaiz(e).getMessage());
            criar(fonte, conexao, false).run(args);
        }
    }

    private static SpringApplication criar(Class<?> fonte, ConexaoOracle conexao, boolean oracle) {
        SpringApplication aplicacao = new SpringApplication(fonte);
        aplicacao.setDefaultProperties(conexao.comoPropriedades());
        if (oracle) {
            aplicacao.setAdditionalProfiles("oracle");
        }
        return aplicacao;
    }

    private static Throwable causaRaiz(Throwable erro) {
        Throwable causa = erro;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa;
    }

    static boolean deveTentarOracle(Map<String, String> ambiente, Properties sistema, String[] args) {
        boolean perfilExplicito = ambiente.containsKey("SPRING_PROFILES_ACTIVE")
                || sistema.containsKey("spring.profiles.active")
                || Arrays.stream(args).anyMatch(arg -> arg.startsWith("--spring.profiles.active"));
        return !perfilExplicito && !"false".equalsIgnoreCase(ambiente.get("ZELO_ORACLE"));
    }

    static boolean oracleDisponivel(ConexaoOracle conexao) {
        Properties propriedades = new Properties();
        propriedades.setProperty("user", conexao.usuario());
        propriedades.setProperty("password", conexao.senha());
        propriedades.setProperty("oracle.net.CONNECT_TIMEOUT", TEMPO_LIMITE_MS);
        propriedades.setProperty("oracle.jdbc.ReadTimeout", TEMPO_LIMITE_MS);
        try (Connection c = DriverManager.getConnection(conexao.url(), propriedades);
             ResultSet tabela = c.getMetaData().getTables(null, c.getSchema(), "T_CH_USUARIO", new String[]{"TABLE"})) {
            if (tabela.next()) {
                return true;
            }
            LOG.warn("O Oracle respondeu, mas o usuário {} não tem as tabelas T_CH_*: rode nele o script do time de BD. "
                    + "Seguindo com o H2 embutido.", conexao.usuario());
        } catch (SQLException e) {
            LOG.warn("Oracle indisponível ({}). Seguindo com o H2 embutido.", e.getMessage());
        }
        return false;
    }
}
