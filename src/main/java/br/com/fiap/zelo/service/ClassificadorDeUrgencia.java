package br.com.fiap.zelo.service;

import br.com.fiap.zelo.domain.enums.NivelUrgencia;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/**
 * Classifica a urgência de um relato por palavras-chave (regra determinística e
 * auditável). O motor de IA do Zelo pertence à disciplina de Disruptive
 * Architectures; esta regra é a linha de base usada pela aplicação Java.
 * A palavra mais grave define o nível; cada palavra adicional soma pontos ao score.
 */
@Component
public class ClassificadorDeUrgencia {

    private static final List<String> PALAVRAS_EMERGENCIA = List.of(
            "sangramento", "sangrando", "sangue", "convulsao", "desmaiou", "desmaio",
            "atropelado", "atropelou", "envenenado", "envenenamento", "intoxicado", "intoxicacao",
            "nao consegue respirar", "parou de respirar", "engasgado", "engasgou",
            "atropelada", "envenenada", "intoxicada", "engasgada", "convulsionando"
    );

    private static final List<String> PALAVRAS_ALTA = List.of(
            "vomito", "vomitando", "diarreia com sangue", "febre alta", "dor intensa",
            "nao anda", "nao consegue andar", "inchaco", "inchado subitamente", "gemendo de dor"
    );

    private static final List<String> PALAVRAS_MEDIA = List.of(
            "nao come", "sem apetite", "apatico", "apatia", "tosse", "espirro",
            "cocando muito", "diarreia", "mancando",
            "apatica", "tossindo", "espirrando"
    );

    private static final BigDecimal SCORE_BASE_EMERGENCIA = new BigDecimal("90");
    private static final BigDecimal SCORE_BASE_ALTA = new BigDecimal("65");
    private static final BigDecimal SCORE_BASE_MEDIA = new BigDecimal("40");
    private static final BigDecimal SCORE_BASE_BAIXA = new BigDecimal("15");
    private static final BigDecimal INCREMENTO_POR_PALAVRA_ADICIONAL = new BigDecimal("3");
    private static final BigDecimal SCORE_MAXIMO = new BigDecimal("100");

    public ClassificacaoUrgencia classificar(String relato) {
        if (relato == null || relato.isBlank()) {
            throw new IllegalArgumentException("O relato da triagem não pode ser vazio.");
        }

        String normalizado = normalizar(relato);

        long ocorrenciasEmergencia = contarOcorrencias(normalizado, PALAVRAS_EMERGENCIA);
        long ocorrenciasAlta = contarOcorrencias(normalizado, PALAVRAS_ALTA);
        long ocorrenciasMedia = contarOcorrencias(normalizado, PALAVRAS_MEDIA);

        NivelUrgencia nivel;
        BigDecimal base;
        long ocorrenciasAdicionais;

        if (ocorrenciasEmergencia > 0) {
            nivel = NivelUrgencia.EMERGENCIA;
            base = SCORE_BASE_EMERGENCIA;
            ocorrenciasAdicionais = ocorrenciasEmergencia - 1 + ocorrenciasAlta + ocorrenciasMedia;
        } else if (ocorrenciasAlta > 0) {
            nivel = NivelUrgencia.ALTA;
            base = SCORE_BASE_ALTA;
            ocorrenciasAdicionais = ocorrenciasAlta - 1 + ocorrenciasMedia;
        } else if (ocorrenciasMedia > 0) {
            nivel = NivelUrgencia.MEDIA;
            base = SCORE_BASE_MEDIA;
            ocorrenciasAdicionais = ocorrenciasMedia - 1;
        } else {
            nivel = NivelUrgencia.BAIXA;
            base = SCORE_BASE_BAIXA;
            ocorrenciasAdicionais = 0;
        }

        BigDecimal score = base.add(INCREMENTO_POR_PALAVRA_ADICIONAL.multiply(BigDecimal.valueOf(Math.max(0, ocorrenciasAdicionais))));
        score = score.min(SCORE_MAXIMO);

        return new ClassificacaoUrgencia(score, nivel);
    }

    private long contarOcorrencias(String textoNormalizado, List<String> palavrasChave) {
        return palavrasChave.stream()
                .filter(textoNormalizado::contains)
                .count();
    }

    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.forLanguageTag("pt-BR"));
    }
}
