package br.com.fiap.zelo.service.painel;

import br.com.fiap.zelo.domain.enums.NivelUrgencia;
import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.domain.enums.TipoAlerta;
import br.com.fiap.zelo.repository.projecao.ContagemAlertas;
import br.com.fiap.zelo.repository.projecao.ContagemPorUrgencia;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Gráficos compartilhados pelos painéis da clínica e do tutor. */
final class GraficoFactory {

    private GraficoFactory() {
    }

    static Grafico porUrgencia(List<ContagemPorUrgencia> linhas) {
        Map<NivelUrgencia, Long> contagem = ContadorPorEnum.contar(NivelUrgencia.class, linhas,
                ContagemPorUrgencia::urgencia, ContagemPorUrgencia::total);
        List<NivelUrgencia> niveis = Arrays.stream(NivelUrgencia.values())
                .sorted(Comparator.comparingInt(NivelUrgencia::getPrioridade))
                .toList();
        return new Grafico(
                niveis.stream().map(Enum::name).toList(),
                niveis.stream().map(NivelUrgencia::getRotulo).toList(),
                List.of(new Grafico.Serie("TRIAGENS", "Triagens", niveis.stream().map(contagem::get).toList())));
    }

    /** Barras empilhadas: uma barra por tipo de cuidado, uma série por status. */
    static Grafico alertasPorTipoEStatus(List<ContagemAlertas> linhas) {
        List<TipoAlerta> tipos = List.of(TipoAlerta.values());
        List<Grafico.Serie> series = Arrays.stream(StatusAlerta.values())
                .map(status -> new Grafico.Serie(status.name(), status.getRotulo(), tipos.stream()
                        .map(tipo -> linhas.stream()
                                .filter(linha -> linha.tipo() == tipo && linha.status() == status)
                                .mapToLong(ContagemAlertas::total)
                                .sum())
                        .toList()))
                .toList();
        return new Grafico(
                tipos.stream().map(Enum::name).toList(),
                tipos.stream().map(TipoAlerta::getRotulo).toList(),
                series);
    }
}
