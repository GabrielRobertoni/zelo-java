package br.com.fiap.zelo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicacao Zelo (Java Advanced - Sprint 3).
 *
 * Zelo: cuidado continuo antes da urgencia. Este modulo cobre a fatia do
 * desafio referente a disciplina de Java Advanced: aplicacao Spring Boot
 * com frontend (Thymeleaf), versionamento de banco via Flyway, autenticacao
 * e autorizacao por perfil (Spring Security) e dois fluxos completos do
 * sistema (Triagem/Encaminhamento e Alertas/Plano de Cuidado).
 */
@SpringBootApplication
public class ZeloApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZeloApplication.class, args);
    }
}
