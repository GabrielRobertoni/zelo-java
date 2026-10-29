package br.com.fiap.zelo.web.suporte;

import br.com.fiap.zelo.exception.ZeloException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.function.Supplier;

/**
 * Padrão das ações de botão (POST + redirect): executa a operação e devolve
 * a mensagem de sucesso, ou a mensagem da regra de negócio violada.
 */
public final class Feedback {

    private Feedback() {
    }

    public static String executar(RedirectAttributes atributos, String destino, String mensagemSucesso,
                                  Runnable operacao) {
        return executar(atributos, destino, () -> {
            operacao.run();
            return mensagemSucesso;
        });
    }

    public static String executar(RedirectAttributes atributos, String destino, Supplier<String> operacao) {
        try {
            atributos.addFlashAttribute("sucesso", operacao.get());
        } catch (ZeloException excecao) {
            atributos.addFlashAttribute("erro", excecao.getMessage());
        }
        return "redirect:" + destino;
    }

    /** Aceita apenas caminhos internos da aplicação, evitando redirecionamento para sites externos. */
    public static String destinoSeguro(String solicitado, String padrao) {
        boolean interno = solicitado != null && solicitado.startsWith("/") && !solicitado.startsWith("//")
                && !solicitado.contains("\\") && !solicitado.contains(":");
        return interno ? solicitado : padrao;
    }
}
