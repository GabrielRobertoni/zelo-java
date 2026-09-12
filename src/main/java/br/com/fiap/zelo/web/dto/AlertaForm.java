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

    @NotNull(message = "Selecione o tipo de alerta.")
    private TipoAlerta tipoAlerta;

    @NotBlank(message = "Informe um titulo para o alerta.")
    @Size(max = 160, message = "O titulo deve ter no maximo 160 caracteres.")
    private String titulo;

    @Size(max = 1000, message = "A mensagem deve ter no maximo 1000 caracteres.")
    private String mensagem;

    @NotNull(message = "Informe a data prevista.")
    @FutureOrPresent(message = "A data prevista nao pode estar no passado.")
    private LocalDate dataPrevista;
}
