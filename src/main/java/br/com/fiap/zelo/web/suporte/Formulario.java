package br.com.fiap.zelo.web.suporte;

import br.com.fiap.zelo.exception.ZeloException;
import org.springframework.validation.BindingResult;

import java.util.function.Supplier;

/**
 * Padrão dos formulários: se a validação passar, executa a ação (que devolve o
 * redirect); se a validação falhar ou uma regra de negócio for violada, a mensagem
 * aparece no próprio formulário, preservando o que o usuário digitou.
 */
public final class Formulario {

    private Formulario() {
    }

    public static String processar(BindingResult validacao, Supplier<String> acao, Supplier<String> reexibir) {
        if (!validacao.hasErrors()) {
            try {
                return acao.get();
            } catch (ZeloException excecao) {
                validacao.reject("regraNegocio", excecao.getMessage());
            }
        }
        return reexibir.get();
    }
}
