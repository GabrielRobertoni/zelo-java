package br.com.fiap.zelo.domain.enums;

/** A prioridade define a ordem da fila de atenção: quanto menor, mais urgente. */
public enum NivelUrgencia {
    BAIXA("Baixa", 3),
    MEDIA("Média", 2),
    ALTA("Alta", 1),
    EMERGENCIA("Emergência", 0);

    private final String rotulo;
    private final int prioridade;

    NivelUrgencia(String rotulo, int prioridade) {
        this.rotulo = rotulo;
        this.prioridade = prioridade;
    }

    public String getRotulo() {
        return rotulo;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public boolean isCritica() {
        return this == ALTA || this == EMERGENCIA;
    }
}
