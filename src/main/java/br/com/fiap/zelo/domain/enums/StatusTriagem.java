package br.com.fiap.zelo.domain.enums;

/**
 * Ciclo de vida da triagem (CHECK CK_T_CH_TRIAGEM_STATUS):
 * ABERTA → ENCAMINHADA → ATENDIDA, ou CANCELADA enquanto em andamento.
 */
public enum StatusTriagem {
    ABERTA("Aberta"),
    ENCAMINHADA("Encaminhada"),
    ATENDIDA("Atendida"),
    CANCELADA("Cancelada");

    private final String rotulo;

    StatusTriagem(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean isEmAndamento() {
        return this == ABERTA || this == ENCAMINHADA;
    }
}
