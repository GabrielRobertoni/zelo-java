package br.com.fiap.zelo.domain.enums;

/**
 * Ciclo de vida do alerta (CHECK CK_T_CH_ALERTA_STATUS):
 * PENDENTE → ENVIADO → CONCLUIDO, ou CANCELADO enquanto aberto.
 */
public enum StatusAlerta {
    PENDENTE("Pendente"),
    ENVIADO("Enviado"),
    CONCLUIDO("Concluído"),
    CANCELADO("Cancelado");

    private final String rotulo;

    StatusAlerta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean isAberto() {
        return this == PENDENTE || this == ENVIADO;
    }
}
