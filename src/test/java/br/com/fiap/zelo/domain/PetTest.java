package br.com.fiap.zelo.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PetTest {

    @Test
    void descreveIdadeEmAnosOuMeses() {
        assertThat(Pet.builder().dataNascimento(LocalDate.now().minusYears(3)).build().getIdadeDescricao()).isEqualTo("3 anos");
        assertThat(Pet.builder().dataNascimento(LocalDate.now().minusYears(1)).build().getIdadeDescricao()).isEqualTo("1 ano");
        assertThat(Pet.builder().dataNascimento(LocalDate.now().minusMonths(5)).build().getIdadeDescricao()).isEqualTo("5 meses");
        assertThat(Pet.builder().build().getIdadeDescricao()).isEqualTo("Idade não informada");
    }
}
