package br.com.fiap.zelo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/** Define o painel de destino de cada perfil após o login (ou ao revisitar /login já autenticado). */
public final class PaginaInicialPorPerfil {

    public static final String PAINEL_TUTOR = "/tutor/painel";
    public static final String PAINEL_CLINICA = "/clinica/painel";

    private PaginaInicialPorPerfil() {
    }

    public static String de(Authentication autenticacao) {
        boolean equipeClinica = autenticacao.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(perfil -> perfil.equals("ROLE_VETERINARIO") || perfil.equals("ROLE_GESTOR"));
        return equipeClinica ? PAINEL_CLINICA : PAINEL_TUTOR;
    }

    public static boolean estaAutenticado(Authentication autenticacao) {
        return autenticacao != null && autenticacao.isAuthenticated() && autenticacao.getPrincipal() instanceof ZeloUserPrincipal;
    }
}
