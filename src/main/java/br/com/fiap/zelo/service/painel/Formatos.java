package br.com.fiap.zelo.service.painel;

import java.time.Duration;

/** Formatação dos valores exibidos nos cards de indicadores. */
final class Formatos {

    static final String SEM_DADOS = "—";

    private Formatos() {
    }

    static String percentual(long parte, long total) {
        return total == 0 ? SEM_DADOS : Math.round(parte * 100.0 / total) + "%";
    }

    static String duracao(Duration duracao) {
        if (duracao == null) {
            return SEM_DADOS;
        }
        long horas = duracao.toHours();
        long minutos = duracao.toMinutesPart();
        return horas == 0 ? minutos + " min" : horas + "h " + String.format("%02d", minutos) + "min";
    }

    static String quantidade(long valor, String singular, String plural) {
        return valor + " " + (valor == 1 ? singular : plural);
    }
}
