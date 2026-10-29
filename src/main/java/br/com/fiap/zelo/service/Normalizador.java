package br.com.fiap.zelo.service;

/** Padroniza textos opcionais antes de persistir (vazio vira nulo, UF em maiúsculas). */
final class Normalizador {

    private Normalizador() {
    }

    static String textoOuNulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    static String uf(String valor) {
        String texto = textoOuNulo(valor);
        return texto == null ? null : texto.toUpperCase();
    }
}
