package br.com.fiap.zelo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

/** Após o login, encaminha cada perfil para o próprio painel. */
public class RedirecionadorPorPerfilSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, PaginaInicialPorPerfil.de(authentication));
    }
}
