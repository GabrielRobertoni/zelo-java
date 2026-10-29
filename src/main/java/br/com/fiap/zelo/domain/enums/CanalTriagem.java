package br.com.fiap.zelo.domain.enums;

public enum CanalTriagem {
    TEXTO("Texto"),
    AUDIO("Áudio"),
    IMAGEM("Imagem");

    private final String rotulo;

    CanalTriagem(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
