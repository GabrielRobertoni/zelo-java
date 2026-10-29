package br.com.fiap.zelo.domain;

import java.security.SecureRandom;

/** Gera códigos de convite legíveis: 8 caracteres, sem letras ou números que se confundem (O/0, I/1). */
public final class CodigoConvite {

    private static final String CARACTERES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int TAMANHO = 8;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private CodigoConvite() {
    }

    public static String gerar() {
        StringBuilder codigo = new StringBuilder(TAMANHO);
        for (int i = 0; i < TAMANHO; i++) {
            codigo.append(CARACTERES.charAt(ALEATORIO.nextInt(CARACTERES.length())));
        }
        return codigo.toString();
    }
}
