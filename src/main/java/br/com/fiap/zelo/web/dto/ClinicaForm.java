package br.com.fiap.zelo.web.dto;

import br.com.fiap.zelo.domain.Clinica;
import br.com.fiap.zelo.web.dto.validacao.Uf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicaForm {

    @NotBlank(message = "Informe o nome da clínica.")
    @Size(max = 160, message = "O nome deve ter no máximo 160 caracteres.")
    private String nome;

    @Pattern(regexp = "^$|^\\d{14}$", message = "Informe o CNPJ com 14 dígitos (somente números) ou deixe em branco.")
    private String cnpj;

    @Size(max = 25, message = "O telefone deve ter no máximo 25 caracteres.")
    private String telefone;

    @Size(max = 220, message = "O endereço deve ter no máximo 220 caracteres.")
    private String endereco;

    @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres.")
    private String cidade;

    @Uf
    private String estado;

    public static ClinicaForm de(Clinica clinica) {
        ClinicaForm form = new ClinicaForm();
        form.setNome(clinica.getNome());
        form.setCnpj(clinica.getCnpj());
        form.setTelefone(clinica.getTelefone());
        form.setEndereco(clinica.getEndereco());
        form.setCidade(clinica.getCidade());
        form.setEstado(clinica.getEstado());
        return form;
    }
}
