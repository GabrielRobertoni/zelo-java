package br.com.fiap.zelo.web.dto.validacao;

import br.com.fiap.zelo.web.dto.DadosAcessoForm;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class SenhasConferemValidator implements ConstraintValidator<SenhasConferem, DadosAcessoForm> {

    @Override
    public boolean isValid(DadosAcessoForm form, ConstraintValidatorContext context) {
        if (form.getSenha() == null || form.getConfirmarSenha() == null
                || Objects.equals(form.getSenha(), form.getConfirmarSenha())) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("confirmarSenha")
                .addConstraintViolation();
        return false;
    }
}
