package br.com.fiap.zelo.security;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;

class ApiTokenServiceTest {
    @Test
    void emiteEValidaTokenComMesmoSegredo() throws Exception {
        ApiTokenService service = create("test-secret");
        String token = service.issue("tutor@zelo.com.br");
        assertThat(service.subject(token)).contains("tutor@zelo.com.br");
    }

    @Test
    void rejeitaTokenAlteradoOuAssinadoComOutroSegredo() throws Exception {
        ApiTokenService service = create("test-secret");
        ApiTokenService other = create("other-secret");
        String token = service.issue("tutor@zelo.com.br");
        assertThat(other.subject(token)).isEmpty();
        String[] parts = token.split("\\.");
        String altered = parts[0] + "." + parts[1] + "x." + parts[2];
        assertThat(service.subject(altered)).isEmpty();
    }

    private ApiTokenService create(String secret) throws Exception {
        Constructor<ApiTokenService> constructor = ApiTokenService.class.getDeclaredConstructor(String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(secret);
    }
}
