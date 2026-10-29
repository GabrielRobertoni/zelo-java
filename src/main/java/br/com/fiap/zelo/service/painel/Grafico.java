package br.com.fiap.zelo.service.painel;

import java.util.List;

/**
 * Dados de um gráfico, independentes da biblioteca de desenho. As chaves
 * (nomes dos enums) permitem ao front-end aplicar a mesma cor de cada nível,
 * status ou tipo em todas as telas.
 */
public record Grafico(List<String> chaves, List<String> rotulos, List<Serie> series) {

    public record Serie(String chave, String nome, List<Long> valores) {
    }

    public long total() {
        return series.stream().flatMap(serie -> serie.valores().stream()).mapToLong(Long::longValue).sum();
    }
}
