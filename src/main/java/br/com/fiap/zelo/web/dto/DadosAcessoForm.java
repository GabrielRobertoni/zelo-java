package br.com.fiap.zelo.web.dto;

import br.com.fiap.zelo.web.dto.validacao.SenhasConferem;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Campos comuns a todo cadastro de usuário, qualquer que seja o perfil. */
@Getter
@Setter
@SenhasConferem
public abstract class DadosAcessoForm {

    @NotBlank(message = "Informe o nome completo.")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 160, message = "O e-mail deve ter no máximo 160 caracteres.")
    private String email;

    @NotBlank(message = "Crie uma senha.")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres.")
    private String senha;

    @NotBlank(message = "Confirme a senha.")
    private String confirmarSenha;
}
