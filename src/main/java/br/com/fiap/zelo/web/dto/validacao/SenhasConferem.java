package br.com.fiap.zelo.web.dto.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Exige que "senha" e "confirmarSenha" sejam iguais; o erro aparece no campo de confirmação. */
@Constraint(validatedBy = SenhasConferemValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SenhasConferem {
    String message() default "As senhas informadas não conferem.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
