package br.com.fiap.zelo.domain.enums;

/** Valores alinhados ao CHECK CK_T_CH_ALERTA_TIPO do DDL do time de Banco de Dados. */
public enum TipoAlerta {
    VACINA("Vacina"),
    CHECKUP("Check-up"),
    RETENCAO("Retenção"),
    POS_CONSULTA("Pós-consulta");

    private final String rotulo;

    TipoAlerta(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
