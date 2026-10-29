package br.com.fiap.zelo.web.dto;

import br.com.fiap.zelo.domain.enums.TipoAlerta;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AlertaForm {

    @NotNull(message = "Selecione o pet.")
    private Long petId;

    @NotNull(message = "Selecione o tipo de cuidado.")
    private TipoAlerta tipoAlerta;

    @NotBlank(message = "Informe um título para o alerta.")
    @Size(max = 160, message = "O título deve ter no máximo 160 caracteres.")
    private String titulo;

    @Size(max = 1000, message = "A mensagem deve ter no máximo 1000 caracteres.")
    private String mensagem;

    @NotNull(message = "Informe a data prevista.")
    @FutureOrPresent(message = "A data prevista não pode estar no passado.")
    private LocalDate dataPrevista;
}
