package br.com.fiap.zelo.domain.enums;

public enum Sexo {
    M("Macho"),
    F("Fêmea"),
    I("Não informado");

    private final String rotulo;

    Sexo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
