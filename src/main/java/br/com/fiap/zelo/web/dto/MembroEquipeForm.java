package br.com.fiap.zelo.web.dto;

import br.com.fiap.zelo.domain.enums.TipoUsuario;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** Cadastro de um novo membro feito pelo gestor, já vinculado à clínica dele. */
@Getter
@Setter
public class MembroEquipeForm extends DadosAcessoForm {

    @NotNull(message = "Selecione o perfil do novo membro.")
    private TipoUsuario tipoUsuario = TipoUsuario.VETERINARIO;
}
