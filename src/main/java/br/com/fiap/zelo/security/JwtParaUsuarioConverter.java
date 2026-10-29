package br.com.fiap.zelo.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Converte o JWT válido no mesmo principal usado pela web ({@link ZeloUserPrincipal}),
 * recarregando o usuário a cada requisição: um acesso desativado deixa de funcionar
 * imediatamente, mesmo com token ainda dentro da validade.
 */
@Component
public class JwtParaUsuarioConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserDetailsService userDetailsService;

    public JwtParaUsuarioConverter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UserDetails usuario = userDetailsService.loadUserByUsername(jwt.getSubject());
        if (!usuario.isEnabled()) {
            throw new DisabledException("Acesso desativado.");
        }
        return UsernamePasswordAuthenticationToken.authenticated(usuario, jwt, usuario.getAuthorities());
    }
}
