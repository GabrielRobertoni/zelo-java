package br.com.fiap.zelo.service.painel;

import br.com.fiap.zelo.domain.Alerta;
import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.Triagem;

import java.util.List;
import java.util.Map;

public record PainelTutor(
        List<Indicador> indicadores,
        Map<String, Grafico> graficos,
        List<SituacaoPet> pets,
        List<Alerta> proximosCuidados,
        List<Triagem> triagensRecentes) {

    /** Resumo de cuidado de um pet: próximo compromisso, última triagem e situação geral. */
    public record SituacaoPet(Pet pet, Alerta proximoCuidado, Triagem ultimaTriagem, Situacao situacao) {
    }

    public enum Situacao {
        ATRASADO("Cuidado atrasado", "critico"),
        ATENCAO("Requer atenção", "atencao"),
        EM_DIA("Em dia", "positivo");

        private final String rotulo;
        private final String tom;

        Situacao(String rotulo, String tom) {
            this.rotulo = rotulo;
            this.tom = tom;
        }

        public String getRotulo() {
            return rotulo;
        }

        public String getTom() {
            return tom;
        }
    }
}
