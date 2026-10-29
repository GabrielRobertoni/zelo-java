package br.com.fiap.zelo.service.painel;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Converte o resultado de um "group by" em um mapa com todas as constantes do enum, inclusive as zeradas. */
final class ContadorPorEnum {

    private ContadorPorEnum() {
    }

    static <E extends Enum<E>, T> Map<E, Long> contar(Class<E> tipo, List<T> linhas,
                                                    Function<T, E> chave, Function<T, Long> total) {
        Map<E, Long> contagem = new EnumMap<>(tipo);
        for (E constante : tipo.getEnumConstants()) {
            contagem.put(constante, 0L);
        }
        linhas.forEach(linha -> contagem.merge(chave.apply(linha), total.apply(linha), Long::sum));
        return contagem;
    }
}
