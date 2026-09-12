package br.com.fiap.zelo.web.advice;

import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Tratamento centralizado de excecoes que escapam dos controllers, evitando
 * que o usuario veja uma tela de erro generica (Whitelabel) durante a
 * demonstracao. Cada controller ja trata a maioria dos casos de negocio
 * localmente (com redirect + flash message); este advice e a rede de
 * seguranca para qualquer caso nao previsto.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String recursoNaoEncontrado(RecursoNaoEncontradoException ex, Model model) {
        model.addAttribute("mensagem", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String regraNegocio(RegraNegocioException ex, Model model) {
        model.addAttribute("mensagem", ex.getMessage());
        return "error/400";
    }

    /**
     * Recurso estatico inexistente (ex.: o navegador pedindo /favicon.ico
     * automaticamente, sem que a aplicacao tenha um arquivo com esse nome).
     * Isso e um 404 comum e esperado - NAO um erro da aplicacao - entao e
     * tratado separadamente do catch-all abaixo para nao poluir o console
     * com stack traces em nivel ERROR a cada requisicao de favicon durante
     * a demonstracao. Registrado apenas em DEBUG.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String recursoEstaticoNaoEncontrado(NoResourceFoundException ex, Model model) {
        log.debug("Recurso estatico nao encontrado: {}", ex.getResourcePath());
        model.addAttribute("mensagem", "Recurso nao encontrado.");
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String erroInesperado(Exception ex, Model model) {
        log.error("Erro inesperado", ex);
        model.addAttribute("mensagem", "Ocorreu um erro inesperado. Tente novamente em instantes.");
        return "error/500";
    }
}
