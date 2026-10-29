package br.com.fiap.zelo.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    @Test
    void iniciaisUsamPrimeiroEUltimoNome() {
        assertThat(Usuario.builder().nome("Ana Gestora").build().getIniciais()).isEqualTo("AG");
        assertThat(Usuario.builder().nome("  bruno da silva vet ").build().getIniciais()).isEqualTo("BV");
    }

    @Test
    void iniciaisDeNomeSimplesOuAusente() {
        assertThat(Usuario.builder().nome("Carla").build().getIniciais()).isEqualTo("C");
        assertThat(Usuario.builder().nome("").build().getIniciais()).isEqualTo("?");
        assertThat(Usuario.builder().build().getIniciais()).isEqualTo("?");
    }
}
