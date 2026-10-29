package br.com.fiap.zelo.web.dto;

import br.com.fiap.zelo.web.dto.validacao.Uf;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistroTutorForm extends DadosAcessoForm {

    @Size(max = 25, message = "O telefone deve ter no máximo 25 caracteres.")
    private String telefone;

    @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres.")
    private String cidade;

    @Uf
    private String estado;
}
