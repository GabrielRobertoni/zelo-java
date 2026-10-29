package br.com.fiap.zelo.domain;

import br.com.fiap.zelo.domain.enums.StatusAlerta;
import br.com.fiap.zelo.domain.enums.TipoAlerta;
import br.com.fiap.zelo.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertaTest {

    private Alerta alerta(LocalDate data) {
        return Alerta.novo(new Pet(), Clinica.builder().id(5L).build(), TipoAlerta.VACINA, "V10", null, data);
    }

    @Test
    void cicloCompletoPendenteEnviadoConcluido() {
        Alerta alerta = alerta(LocalDate.now());
        assertThat(alerta.getStatusAlerta()).isEqualTo(StatusAlerta.PENDENTE);
        alerta.marcarComoEnviado();
        assertThat(alerta.getDataEnvio()).isNotNull();
        alerta.concluir();
        assertThat(alerta.getStatusAlerta()).isEqualTo(StatusAlerta.CONCLUIDO);
    }

    @Test
    void soEnviaAlertaPendente() {
        Alerta alerta = alerta(LocalDate.now());
        alerta.marcarComoEnviado();
        assertThatThrownBy(alerta::marcarComoEnviado).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoAlteraAlertaFinalizado() {
        Alerta alerta = alerta(LocalDate.now());
        alerta.cancelar();
        assertThatThrownBy(alerta::concluir).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(alerta::cancelar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void atrasadoQuandoAbertoEComDataPassada() {
        assertThat(alerta(LocalDate.now().minusDays(1)).isAtrasado()).isTrue();
        assertThat(alerta(LocalDate.now()).isAtrasado()).isFalse();
        Alerta concluido = alerta(LocalDate.now().minusDays(1));
        concluido.concluir();
        assertThat(concluido.isAtrasado()).isFalse();
    }

    @Test
    void identificaClinicaDeOrigem() {
        Alerta alerta = alerta(LocalDate.now());
        assertThat(alerta.isRecomendadoPorClinica()).isTrue();
        assertThat(alerta.pertenceAClinica(5L)).isTrue();
        assertThat(Alerta.novo(new Pet(), null, TipoAlerta.CHECKUP, "x", null, LocalDate.now()).pertenceAClinica(5L)).isFalse();
    }
}
