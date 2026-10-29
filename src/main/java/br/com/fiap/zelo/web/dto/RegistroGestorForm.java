package br.com.fiap.zelo.web.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

/** Cadastro do gestor junto com a clínica que ele passa a administrar. */
@Getter
@Setter
public class RegistroGestorForm extends DadosAcessoForm {

    @Valid
    private ClinicaForm clinica = new ClinicaForm();
}
