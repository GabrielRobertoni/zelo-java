package br.com.fiap.zelo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/** Emite o JWT (HS256) usado pelo aplicativo mobile para chamar a API REST. */
@Service
public class TokenApiService {

    public static final String EMISSOR = "zelo-java";

    private final JwtEncoder codificador;
    private final Duration validade;
    private final Clock relogio;

    public TokenApiService(JwtEncoder codificador, @Value("${zelo.api.jwt-validade-horas}") long validadeHoras,
                           Clock relogio) {
        this.codificador = codificador;
        this.validade = Duration.ofHours(validadeHoras);
        this.relogio = relogio;
    }

    public String emitir(ZeloUserPrincipal usuario) {
        Instant agora = relogio.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(usuario.getUsername())
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .claim("perfil", usuario.getUsuario().getTipoUsuario().name())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return codificador.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }
}
