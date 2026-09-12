package br.com.fiap.zelo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Apos o login, encaminha cada perfil para o seu proprio painel:
 *  - TUTOR -> /tutor/dashboard
 *  - VETERINARIO ou GESTOR -> /clinica/dashboard
 */
public class RedirecionadorPorPerfilSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        boolean ehClinica = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_VETERINARIO") || a.equals("ROLE_GESTOR"));

        String destino = ehClinica ? "/clinica/dashboard" : "/tutor/dashboard";
        getRedirectStrategy().sendRedirect(request, response, destino);
    }
}
