package br.com.fiap.zelo.web.dto;

import br.com.fiap.zelo.domain.enums.CanalTriagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriagemForm {

    @NotNull(message = "Selecione o pet.")
    private Long petId;

    @NotNull(message = "Selecione a clinica de destino.")
    private Long clinicaId;

    @NotNull(message = "Selecione o canal do relato.")
    private CanalTriagem canal;

    @NotBlank(message = "Descreva o que esta acontecendo com o pet.")
    @Size(min = 10, max = 2000, message = "O relato deve ter entre 10 e 2000 caracteres.")
    private String relato;
}
