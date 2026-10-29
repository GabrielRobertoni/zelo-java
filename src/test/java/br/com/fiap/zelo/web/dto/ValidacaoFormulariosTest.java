package br.com.fiap.zelo.web.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidacaoFormulariosTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private RegistroTutorForm tutorValido() {
        RegistroTutorForm form = new RegistroTutorForm();
        form.setNome("Maria");
        form.setEmail("maria@exemplo.com");
        form.setSenha("segredo1");
        form.setConfirmarSenha("segredo1");
        return form;
    }

    private Set<String> camposInvalidos(Object form) {
        return validator.validate(form).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void cadastroValidoNaoTemErros() {
        assertThat(camposInvalidos(tutorValido())).isEmpty();
    }

    @Test
    void senhasDiferentesMarcamOCampoDeConfirmacao() {
        RegistroTutorForm form = tutorValido();
        form.setConfirmarSenha("outra-senha");
        assertThat(camposInvalidos(form)).containsExactly("confirmarSenha");
    }

    @Test
    void ufPrecisaTerDuasLetras() {
        RegistroTutorForm form = tutorValido();
        form.setEstado("SAO");
        assertThat(camposInvalidos(form)).containsExactly("estado");
        form.setEstado("sp");
        assertThat(camposInvalidos(form)).isEmpty();
        form.setEstado("");
        assertThat(camposInvalidos(form)).isEmpty();
    }

    @Test
    void emailESenhaCurtaSaoValidados() {
        RegistroTutorForm form = tutorValido();
        form.setEmail("nao-e-email");
        form.setSenha("123");
        form.setConfirmarSenha("123");
        assertThat(camposInvalidos(form)).containsExactlyInAnyOrder("email", "senha");
    }

    @Test
    void gestorValidaTambemOsDadosDaClinica() {
        RegistroGestorForm form = new RegistroGestorForm();
        form.setNome("Ana");
        form.setEmail("ana@exemplo.com");
        form.setSenha("segredo1");
        form.setConfirmarSenha("segredo1");
        form.getClinica().setCnpj("123");
        assertThat(camposInvalidos(form)).containsExactlyInAnyOrder("clinica.nome", "clinica.cnpj");
    }

    @Test
    void petNaoAceitaPesoZeroNemNascimentoFuturo() {
        PetForm form = new PetForm();
        form.setNome("Rex");
        form.setEspecie(br.com.fiap.zelo.domain.enums.Especie.CAO);
        form.setPesoKg(BigDecimal.ZERO);
        form.setDataNascimento(LocalDate.now().plusDays(1));
        assertThat(camposInvalidos(form)).containsExactlyInAnyOrder("pesoKg", "dataNascimento");
    }

    @Test
    void alertaNaoAceitaDataPassada() {
        AlertaForm form = new AlertaForm();
        form.setPetId(1L);
        form.setTipoAlerta(br.com.fiap.zelo.domain.enums.TipoAlerta.VACINA);
        form.setTitulo("V10");
        form.setDataPrevista(LocalDate.now().minusDays(1));
        assertThat(camposInvalidos(form)).containsExactly("dataPrevista");
    }

    @Test
    void relatoDeTriagemPrecisaDeDezCaracteres() {
        TriagemForm form = new TriagemForm();
        form.setPetId(1L);
        form.setClinicaId(1L);
        form.setRelato("curto");
        assertThat(camposInvalidos(form)).containsExactly("relato");
    }
}
