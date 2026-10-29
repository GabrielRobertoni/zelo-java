package br.com.fiap.zelo.web.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;

/**
 * Valida na API os mesmos formulários usados pelas telas, para que web e
 * mobile sigam exatamente as mesmas regras e mensagens.
 */
@Component
class ApiFormularios {

    private final Validator validator;

    ApiFormularios(Validator validator) {
        this.validator = validator;
    }

    <T> T validar(T formulario) throws BindException {
        BeanPropertyBindingResult resultado = new BeanPropertyBindingResult(formulario, "form");
        for (ConstraintViolation<T> violacao : validator.validate(formulario)) {
            resultado.rejectValue(violacao.getPropertyPath().toString(), "invalido", violacao.getMessage());
        }
        if (resultado.hasErrors()) {
            throw new BindException(resultado);
        }
        return formulario;
    }
}
