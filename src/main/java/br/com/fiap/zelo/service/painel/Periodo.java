package br.com.fiap.zelo.service.painel;

import java.time.LocalDate;
import java.util.Arrays;

/** Janela de análise do painel da clínica. */
public enum Periodo {
    SETE_DIAS(7),
    TRINTA_DIAS(30),
    NOVENTA_DIAS(90);

    public static final Periodo PADRAO = TRINTA_DIAS;

    private final int dias;

    Periodo(int dias) {
        this.dias = dias;
    }

    public int getDias() {
        return dias;
    }

    public String getRotulo() {
        return "Últimos " + dias + " dias";
    }

    /** Primeiro dia da janela; o dia de hoje conta como o último. */
    public LocalDate inicio(LocalDate hoje) {
        return hoje.minusDays(dias - 1L);
    }

    /** Converte o parâmetro da URL; valores ausentes ou inválidos usam o período padrão. */
    public static Periodo deDias(String dias) {
        return Arrays.stream(values())
                .filter(periodo -> String.valueOf(periodo.dias).equals(dias))
                .findFirst()
                .orElse(PADRAO);
    }
}
