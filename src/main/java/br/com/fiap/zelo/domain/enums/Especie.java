package br.com.fiap.zelo.domain.enums;

/** Valores alinhados ao CHECK CK_T_CH_PET_ESPECIE do DDL do time de Banco de Dados. */
public enum Especie {
    CAO("Cão"),
    GATO("Gato"),
    OUTRO("Outro");

    private final String rotulo;

    Especie(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
