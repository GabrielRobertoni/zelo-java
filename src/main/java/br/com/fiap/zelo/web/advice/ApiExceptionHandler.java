package br.com.fiap.zelo.web.advice;

import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.web.api.ApiModels.ApiErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/** Erros da API REST sempre em JSON, com o status HTTP correspondente. */
@RestControllerAdvice(basePackages = "br.com.fiap.zelo.web.api")
public class ApiExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> validacao(BindException excecao) {
        Map<String, String> erros = new LinkedHashMap<>();
        excecao.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        excecao.getBindingResult().getGlobalErrors()
                .forEach(erro -> erros.putIfAbsent(erro.getObjectName(), erro.getDefaultMessage()));
        return responder(HttpStatus.BAD_REQUEST, "Revise os campos destacados e tente novamente.", erros);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiErrorResponse> requisicaoInvalida() {
        return responder(HttpStatus.BAD_REQUEST, "Os dados enviados estão em formato inválido.", Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> credenciais() {
        return responder(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos, ou acesso desativado.", Map.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiErrorResponse> regraNegocio(RegraNegocioException excecao) {
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getMessage(), Map.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> naoEncontrado(RecursoNaoEncontradoException excecao) {
        return responder(HttpStatus.NOT_FOUND, excecao.getMessage(), Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> conflito() {
        return responder(HttpStatus.CONFLICT, "A operação conflita com dados já existentes.", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> inesperado(Exception excecao) {
        LOG.error("Erro inesperado na API", excecao);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível concluir a solicitação agora.", Map.of());
    }

    private ResponseEntity<ApiErrorResponse> responder(HttpStatus status, String mensagem, Map<String, String> erros) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(mensagem, status.value(), erros));
    }
}
