package br.com.fiap.zelo.web.dto.validacao;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Sigla de UF opcional: vazia ou exatamente duas letras. */
@Pattern(regexp = "^$|^[A-Za-z]{2}$")
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Uf {
    String message() default "Informe a UF com 2 letras (ex.: SP).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
