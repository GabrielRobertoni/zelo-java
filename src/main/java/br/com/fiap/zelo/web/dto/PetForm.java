package br.com.fiap.zelo.web.dto;

import br.com.fiap.zelo.domain.Pet;
import br.com.fiap.zelo.domain.enums.Especie;
import br.com.fiap.zelo.domain.enums.Sexo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PetForm {

    @NotBlank(message = "Informe o nome do pet.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotNull(message = "Selecione a espécie do pet.")
    private Especie especie;

    @Size(max = 80, message = "A raça deve ter no máximo 80 caracteres.")
    private String raca;

    private Sexo sexo;

    @PastOrPresent(message = "A data de nascimento não pode estar no futuro.")
    private LocalDate dataNascimento;

    @DecimalMin(value = "0.01", message = "O peso deve ser maior que zero.")
    @DecimalMax(value = "999.99", message = "Informe um peso de até 999,99 kg.")
    private BigDecimal pesoKg;

    private boolean castrado;

    public static PetForm de(Pet pet) {
        PetForm form = new PetForm();
        form.setNome(pet.getNome());
        form.setEspecie(pet.getEspecie());
        form.setRaca(pet.getRaca());
        form.setSexo(pet.getSexo());
        form.setDataNascimento(pet.getDataNascimento());
        form.setPesoKg(pet.getPesoKg());
        form.setCastrado(pet.isCastrado());
        return form;
    }
}
