package br.com.fiap.zelo.domain;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ClinicaTest {

    @Test
    void codigoDeConviteTemOitoCaracteresSemAmbiguidade() {
        assertThat(CodigoConvite.gerar()).matches("[A-HJ-NP-Z2-9]{8}");
    }

    @Test
    void codigosGeradosVariam() {
        assertThat(Stream.generate(CodigoConvite::gerar).limit(50).distinct().count()).isGreaterThan(45);
    }

    @Test
    void alternaDisponibilidade() {
        Clinica clinica = Clinica.builder().build();
        assertThat(clinica.isAtiva()).isTrue();
        clinica.alternarDisponibilidade();
        assertThat(clinica.isAtiva()).isFalse();
    }

    @Test
    void montaLocalizacao() {
        assertThat(Clinica.builder().cidade("São Paulo").estado("SP").build().getLocalizacao()).isEqualTo("São Paulo/SP");
        assertThat(Clinica.builder().cidade("Osasco").build().getLocalizacao()).isEqualTo("Osasco");
        assertThat(Clinica.builder().build().getLocalizacao()).isEmpty();
    }
}
