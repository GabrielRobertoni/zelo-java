package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.enums.NivelUrgencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testa a regra de negocio deterministica de classificacao de urgencia
 * (fluxo "Triagem e Encaminhamento"). Nao depende de Spring nem de banco de
 * dados - e uma unidade pura, o que a torna a mais rapida e confiavel da
 * suite.
 */
class ClassificadorDeUrgenciaTest {

    private final ClassificadorDeUrgencia classificador = new ClassificadorDeUrgencia();

    @Test
    void relatoComPalavraDeEmergenciaClassificaComoEmergenciaEScoreAlto() {
        var resultado = classificador.classificar("Meu cachorro esta sangrando muito e nao para");

        assertThat(resultado.nivelUrgencia()).isEqualTo(NivelUrgencia.EMERGENCIA);
        assertThat(resultado.scoreRisco()).isEqualByComparingTo(new BigDecimal("90"));
    }

    @Test
    void relatoComPalavraDeAltaUrgenciaClassificaComoAlta() {
        var resultado = classificador.classificar("O gato esta vomitando desde ontem a noite");

        assertThat(resultado.nivelUrgencia()).isEqualTo(NivelUrgencia.ALTA);
        assertThat(resultado.scoreRisco()).isEqualByComparingTo(new BigDecimal("65"));
    }

    @Test
    void relatoComPalavraDeMediaUrgenciaClassificaComoMedia() {
        var resultado = classificador.classificar("Ele esta sem apetite ha dois dias");

        assertThat(resultado.nivelUrgencia()).isEqualTo(NivelUrgencia.MEDIA);
        assertThat(resultado.scoreRisco()).isEqualByComparingTo(new BigDecimal("40"));
    }

    @Test
    void relatoSemPalavrasChaveClassificaComoBaixa() {
        var resultado = classificador.classificar("Rotina de acompanhamento, sem sintomas aparentes");

        assertThat(resultado.nivelUrgencia()).isEqualTo(NivelUrgencia.BAIXA);
        assertThat(resultado.scoreRisco()).isEqualByComparingTo(new BigDecimal("15"));
    }

    @Test
    void classificacaoIgnoraAcentuacaoECaixaDeTexto() {
        // "convulsão" com acento e em maiusculas deve ser reconhecida
        // exatamente como "convulsao" (normalizacao NFD + lower-case pt-BR).
        var resultado = classificador.classificar("O PET TEVE UMA CONVULSÃO agora ha pouco");

        assertThat(resultado.nivelUrgencia()).isEqualTo(NivelUrgencia.EMERGENCIA);
    }

    @Test
    void palavrasChaveAdicionaisAumentamOScoreSemUltrapassarCemLimitado() {
        // "sangrando" (emergencia) + "vomitando" (alta) + "tosse" (media) =
        // 1 ocorrencia de emergencia (base) + 2 ocorrencias adicionais (+3 cada).
        var resultado = classificador.classificar("Esta sangrando, vomitando e com tosse forte");

        assertThat(resultado.nivelUrgencia()).isEqualTo(NivelUrgencia.EMERGENCIA);
        assertThat(resultado.scoreRisco()).isEqualByComparingTo(new BigDecimal("96"));
    }

    @Test
    void scoreNuncaUltrapassaCem() {
        String relatoComMuitasPalavrasGraves =
                "Sangrando, sangue, convulsao, desmaiou, atropelado, envenenado, intoxicado, engasgado";

        var resultado = classificador.classificar(relatoComMuitasPalavrasGraves);

        assertThat(resultado.scoreRisco()).isEqualByComparingTo(new BigDecimal("100"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void relatoVazioOuEmBrancoLancaExcecao(String relatoInvalido) {
        assertThatThrownBy(() -> classificador.classificar(relatoInvalido))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void relatoNuloLancaExcecao() {
        assertThatThrownBy(() -> classificador.classificar(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
