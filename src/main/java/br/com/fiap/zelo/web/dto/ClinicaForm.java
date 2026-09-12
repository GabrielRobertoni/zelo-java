package br.com.fiap.zelo.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicaForm {

    @NotBlank(message = "Informe o nome da clinica.")
    @Size(max = 160, message = "O nome deve ter no maximo 160 caracteres.")
    private String nome;

    @Pattern(regexp = "^$|^\\d{14}$", message = "Informe um CNPJ valido com 14 digitos (somente numeros) ou deixe em branco.")
    private String cnpj;

    @Size(max = 25, message = "O telefone deve ter no maximo 25 caracteres.")
    private String telefone;

    @Size(max = 220, message = "O endereco deve ter no maximo 220 caracteres.")
    private String endereco;

    @Size(max = 80, message = "A cidade deve ter no maximo 80 caracteres.")
    private String cidade;

    @Pattern(regexp = "^$|^[A-Za-z]{2}$", message = "Informe a UF com 2 letras.")
    private String estado;
}
