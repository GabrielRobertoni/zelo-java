package br.com.fiap.zelo.config;

import br.com.fiap.zelo.config.SelecaoDeBanco.ConexaoOracle;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class SelecaoDeBancoTest {

    private static final String[] SEM_ARGUMENTOS = {};

    @Test
    void usaOOracleDoGrupoQuandoNadaFoiConfigurado() {
        ConexaoOracle conexao = ConexaoOracle.de(Map.of());

        assertThat(conexao.url()).isEqualTo(SelecaoDeBanco.URL_PADRAO);
        assertThat(conexao.usuario()).isEqualTo(SelecaoDeBanco.USUARIO_PADRAO);
        assertThat(SelecaoDeBanco.deveTentarOracle(Map.of(), new Properties(), SEM_ARGUMENTOS)).isTrue();
    }

    @Test
    void variaveisDeAmbienteSubstituemOsDadosPadrao() {
        ConexaoOracle conexao = ConexaoOracle.de(Map.of(
                "ORACLE_URL", "jdbc:oracle:thin:@//outro:1521/XE", "ORACLE_USER", "rm1", "ORACLE_PASSWORD", "s"));

        assertThat(conexao.comoPropriedades()).containsEntry("zelo.oracle.url", "jdbc:oracle:thin:@//outro:1521/XE")
                .containsEntry("zelo.oracle.usuario", "rm1")
                .containsEntry("zelo.oracle.senha", "s");
    }

    @Test
    void respeitaPerfilEscolhidoExplicitamente() {
        assertThat(SelecaoDeBanco.deveTentarOracle(Map.of("SPRING_PROFILES_ACTIVE", "dev"), new Properties(), SEM_ARGUMENTOS))
                .isFalse();
        assertThat(SelecaoDeBanco.deveTentarOracle(Map.of(), new Properties(), new String[]{"--spring.profiles.active=dev"}))
                .isFalse();
        Properties sistema = new Properties();
        sistema.setProperty("spring.profiles.active", "dev");
        assertThat(SelecaoDeBanco.deveTentarOracle(Map.of(), sistema, SEM_ARGUMENTOS)).isFalse();
    }

    @Test
    void zeloOracleFalseForcaOH2() {
        assertThat(SelecaoDeBanco.deveTentarOracle(Map.of("ZELO_ORACLE", "false"), new Properties(), SEM_ARGUMENTOS))
                .isFalse();
    }

    @Test
    void oracleForaDoArNaoImpedeAAplicacaoDeSubir() {
        ConexaoOracle inalcancavel = new ConexaoOracle("jdbc:oracle:thin:@//127.0.0.1:1/NADA", "x", "y");

        assertThat(SelecaoDeBanco.oracleDisponivel(inalcancavel)).isFalse();
    }
}
