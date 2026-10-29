package br.com.fiap.zelo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Cadastro de veterinário: entra na equipe da clínica pelo código de convite do gestor. */
@Getter
@Setter
public class RegistroVeterinarioForm extends DadosAcessoForm {

    @NotBlank(message = "Informe o código de convite fornecido pelo gestor da clínica.")
    @Size(max = 12, message = "O código de convite tem no máximo 12 caracteres.")
    private String codigoConvite;
}
