package br.com.fiap.zelo.web.suporte;

import br.com.fiap.zelo.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;

class FeedbackTest {

    @Test
    void sucessoGeraMensagemDeSucessoERedirect() {
        RedirectAttributesModelMap atributos = new RedirectAttributesModelMap();

        String destino = Feedback.executar(atributos, "/tutor/alertas", "Feito!", () -> { });

        assertThat(destino).isEqualTo("redirect:/tutor/alertas");
        assertThat(atributos.getFlashAttributes().get("sucesso")).isEqualTo("Feito!");
    }

    @Test
    void regraDeNegocioViraMensagemDeErro() {
        RedirectAttributesModelMap atributos = new RedirectAttributesModelMap();

        Feedback.executar(atributos, "/x", "Feito!", () -> {
            throw new RegraNegocioException("Não pode.");
        });

        assertThat(atributos.getFlashAttributes().get("erro")).isEqualTo("Não pode.");
        assertThat(atributos.getFlashAttributes()).doesNotContainKey("sucesso");
    }

    @Test
    void aceitaCaminhoInterno() {
        assertThat(Feedback.destinoSeguro("/clinica/painel?periodo=7", "/padrao")).isEqualTo("/clinica/painel?periodo=7");
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://site-malicioso.com", "//site-malicioso.com", "/\\site", "javascript:alert(1)", ""})
    void recusaDestinoExterno(String destino) {
        assertThat(Feedback.destinoSeguro(destino, "/padrao")).isEqualTo("/padrao");
    }

    @Test
    void usaPadraoQuandoNaoInformado() {
        assertThat(Feedback.destinoSeguro(null, "/padrao")).isEqualTo("/padrao");
    }
}
