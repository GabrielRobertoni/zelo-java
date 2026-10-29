package br.com.fiap.zelo.config;

import br.com.fiap.zelo.security.JwtParaUsuarioConverter;
import br.com.fiap.zelo.security.RedirecionadorPorPerfilSuccessHandler;
import br.com.fiap.zelo.security.RespostaSegurancaApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Duas cadeias de segurança independentes:
 *  1. API REST do aplicativo mobile (/api/**): sem sessão, autenticada por JWT, respostas 401/403 em JSON.
 *  2. Aplicação web: sessão com formulário de login, CSRF ativo e rotas protegidas por perfil.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] ROTAS_PUBLICAS = {
            "/", "/login", "/registro", "/registro/**", "/css/**", "/js/**", "/webjars/**", "/favicon.svg",
            "/actuator/health", "/error"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain segurancaApi(HttpSecurity http, JwtParaUsuarioConverter conversor,
                                           RespostaSegurancaApi respostas) throws Exception {
        return http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/pets/**", "/api/triagens/**", "/api/alertas/**").hasRole("TUTOR")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(recurso -> recurso
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversor))
                        .authenticationEntryPoint(respostas)
                        .accessDeniedHandler(respostas))
                .exceptionHandling(erros -> erros.authenticationEntryPoint(respostas).accessDeniedHandler(respostas))
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain segurancaWeb(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(rotas -> rotas
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/clinica/gerenciar/**").hasRole("GESTOR")
                        .requestMatchers("/clinica/**").hasAnyRole("VETERINARIO", "GESTOR")
                        .requestMatchers("/tutor/**").hasRole("TUTOR")
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .successHandler(new RedirecionadorPorPerfilSuccessHandler())
                        .failureUrl("/login?erro")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?sair")
                        .permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(cabecalhos -> cabecalhos.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .build();
    }

    /** O aplicativo mobile (Expo) roda em outra origem e envia o token no cabeçalho Authorization. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOriginPatterns(List.of("*"));
        configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuracao.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
        fonte.registerCorsConfiguration("/api/**", configuracao);
        return fonte;
    }
}
