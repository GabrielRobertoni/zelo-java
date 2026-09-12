package br.com.fiap.zelo.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroTutorForm {

    @NotBlank(message = "Informe seu nome completo.")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
    private String nome;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um e-mail valido.")
    @Size(max = 160, message = "O e-mail deve ter no maximo 160 caracteres.")
    private String email;

    @NotBlank(message = "Crie uma senha.")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres.")
    private String senha;

    @NotBlank(message = "Confirme a senha.")
    private String confirmarSenha;

    @Size(max = 25, message = "O telefone deve ter no maximo 25 caracteres.")
    private String telefone;

    @Size(max = 80, message = "A cidade deve ter no maximo 80 caracteres.")
    private String cidade;

    private String estado;

    public boolean senhasConferem() {
        return senha != null && senha.equals(confirmarSenha);
    }
}
