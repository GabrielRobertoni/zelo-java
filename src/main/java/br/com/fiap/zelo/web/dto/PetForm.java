package br.com.fiap.zelo.web.dto;

import br.com.fiap.zelo.domain.enums.Especie;
import br.com.fiap.zelo.domain.enums.Sexo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PetForm {

    @NotBlank(message = "Informe o nome do pet.")
    @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres.")
    private String nome;

    @NotNull(message = "Selecione a especie do pet.")
    private Especie especie;

    @Size(max = 80, message = "A raca deve ter no maximo 80 caracteres.")
    private String raca;

    private Sexo sexo;

    @Past(message = "A data de nascimento deve estar no passado.")
    private LocalDate dataNascimento;

    @DecimalMin(value = "0.01", message = "O peso deve ser maior que zero.")
    @DecimalMax(value = "999.99", message = "Informe um peso valido.")
    private BigDecimal pesoKg;

    private boolean castrado;
}
