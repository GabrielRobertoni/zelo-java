package br.com.fiap.zelo.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ApiTokenService {
    private static final String HEADER = "{\"alg\":\"HS256\",\"typ\":\"ZEL0\"}";
    private static final Pattern SUBJECT = Pattern.compile("\\\"sub\\\":\\\"([^\\\"]+)\\\"");
    private static final Pattern EXPIRATION = Pattern.compile("\\\"exp\\\":(\\d+)");

    private final String secret;

    public ApiTokenService(@Value("${zelo.api.token-secret:change-this-secret-in-production}") String secret) {
        this.secret = secret;
    }

    public String issue(String subject) {
        long expiration = Instant.now().plusSeconds(60L * 60L * 24L * 7L).getEpochSecond();
        String header = encode(HEADER);
        String payload = encode("{\"sub\":\"" + subject + "\",\"exp\":" + expiration + "}");
        return header + "." + payload + "." + sign(header + "." + payload);
    }

    public Optional<String> subject(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3 || !constantTimeEquals(sign(parts[0] + "." + parts[1]), parts[2])) return Optional.empty();
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Matcher exp = EXPIRATION.matcher(payload);
            Matcher subject = SUBJECT.matcher(payload);
            if (!exp.find() || !subject.find() || Instant.now().getEpochSecond() >= Long.parseLong(exp.group(1))) return Optional.empty();
            return Optional.of(subject.group(1));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Nao foi possivel assinar o token da API.", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(left.getBytes(StandardCharsets.UTF_8), right.getBytes(StandardCharsets.UTF_8));
    }
}
