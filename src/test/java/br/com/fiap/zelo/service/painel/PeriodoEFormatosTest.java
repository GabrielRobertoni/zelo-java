package br.com.fiap.zelo.service.painel;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodoEFormatosTest {

    @Test
    void periodoInvalidoUsaOPadrao() {
        assertThat(Periodo.deDias("7")).isEqualTo(Periodo.SETE_DIAS);
        assertThat(Periodo.deDias("13")).isEqualTo(Periodo.TRINTA_DIAS);
        assertThat(Periodo.deDias("abc")).isEqualTo(Periodo.TRINTA_DIAS);
        assertThat(Periodo.deDias(null)).isEqualTo(Periodo.TRINTA_DIAS);
    }

    @Test
    void periodoIncluiODiaDeHoje() {
        LocalDate hoje = LocalDate.of(2026, 10, 6);
        assertThat(Periodo.SETE_DIAS.inicio(hoje)).isEqualTo(LocalDate.of(2026, 9, 30));
    }

    @Test
    void formataPercentualEDuracao() {
        assertThat(Formatos.percentual(1, 3)).isEqualTo("33%");
        assertThat(Formatos.percentual(0, 0)).isEqualTo(Formatos.SEM_DADOS);
        assertThat(Formatos.duracao(Duration.ofMinutes(125))).isEqualTo("2h 05min");
        assertThat(Formatos.duracao(Duration.ofMinutes(40))).isEqualTo("40 min");
        assertThat(Formatos.duracao(null)).isEqualTo(Formatos.SEM_DADOS);
        assertThat(Formatos.quantidade(1, "caso", "casos")).isEqualTo("1 caso");
    }
}
