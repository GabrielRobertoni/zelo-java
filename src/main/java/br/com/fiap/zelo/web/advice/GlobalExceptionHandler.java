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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Rede de segurança das telas: qualquer erro vira uma página amigável, nunca a tela genérica do servidor. */
@ControllerAdvice(basePackages = "br.com.fiap.zelo.web.controller")
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String MENSAGEM = "mensagem";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String recursoNaoEncontrado(RecursoNaoEncontradoException excecao, Model model) {
        model.addAttribute(MENSAGEM, excecao.getMessage());
        return "error/404";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String enderecoInvalido(Model model) {
        model.addAttribute(MENSAGEM, "O endereço acessado não existe.");
        return "error/404";
    }

    @ExceptionHandler(RegraNegocioException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String regraNegocio(RegraNegocioException excecao, Model model) {
        model.addAttribute(MENSAGEM, excecao.getMessage());
        return "error/400";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String erroInesperado(Exception excecao, Model model) {
        LOG.error("Erro inesperado ao processar a requisição", excecao);
        model.addAttribute(MENSAGEM, "Ocorreu um erro inesperado. Tente novamente em instantes.");
        return "error/500";
    }
}
