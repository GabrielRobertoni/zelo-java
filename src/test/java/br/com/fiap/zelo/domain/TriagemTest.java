package br.com.fiap.zelo.domain;

import br.com.fiap.zelo.domain.enums.CanalTriagem;
import br.com.fiap.zelo.domain.enums.NivelUrgencia;
import br.com.fiap.zelo.domain.enums.StatusTriagem;
import br.com.fiap.zelo.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriagemTest {

    private Triagem novaTriagem() {
        Clinica clinica = Clinica.builder().id(1L).build();
        Tutor tutor = Tutor.builder().id(10L).build();
        return Triagem.abrir(new Pet(), tutor, clinica, CanalTriagem.TEXTO, "Relato de teste",
                BigDecimal.TEN, NivelUrgencia.BAIXA);
    }

    @Test
    void triagemNasceAberta() {
        assertThat(novaTriagem().getStatusTriagem()).isEqualTo(StatusTriagem.ABERTA);
    }

    @Test
    void encaminharRegistraDataEMudaStatus() {
        Triagem triagem = novaTriagem();
        triagem.encaminhar();
        assertThat(triagem.getStatusTriagem()).isEqualTo(StatusTriagem.ENCAMINHADA);
        assertThat(triagem.getEncaminhadaEm()).isNotNull();
    }

    @Test
    void soEncaminhaTriagemAberta() {
        Triagem triagem = novaTriagem();
        triagem.encaminhar();
        assertThatThrownBy(triagem::encaminhar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void concluiTriagemAbertaOuEncaminhada() {
        Triagem aberta = novaTriagem();
        aberta.concluir();
        Triagem encaminhada = novaTriagem();
        encaminhada.encaminhar();
        encaminhada.concluir();
        assertThat(aberta.getStatusTriagem()).isEqualTo(StatusTriagem.ATENDIDA);
        assertThat(encaminhada.getStatusTriagem()).isEqualTo(StatusTriagem.ATENDIDA);
    }

    @Test
    void naoAlteraTriagemFinalizada() {
        Triagem triagem = novaTriagem();
        triagem.concluir();
        assertThatThrownBy(triagem::cancelar).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(triagem::concluir).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void tutorSoCancelaEnquantoAberta() {
        Triagem triagem = novaTriagem();
        triagem.encaminhar();
        assertThatThrownBy(triagem::cancelarPeloTutor)
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("encaminhada");
    }

    @Test
    void verificaDonoEClinica() {
        Triagem triagem = novaTriagem();
        assertThat(triagem.pertenceAClinica(1L)).isTrue();
        assertThat(triagem.pertenceAClinica(2L)).isFalse();
        assertThat(triagem.pertenceAoTutor(10L)).isTrue();
        assertThat(triagem.pertenceAoTutor(11L)).isFalse();
    }
}
