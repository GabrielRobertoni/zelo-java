package br.com.fiap.zelo.web.advice;

import br.com.fiap.zelo.exception.RecursoNaoEncontradoException;
import br.com.fiap.zelo.exception.RegraNegocioException;
import br.com.fiap.zelo.web.api.ApiModels;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackageClasses = br.com.fiap.zelo.web.api.ApiAuthController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ApiModels.ApiErrorResponse> validation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "Revise os campos destacados e tente novamente.", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public org.springframework.http.ResponseEntity<ApiModels.ApiErrorResponse> invalidJson(HttpMessageNotReadableException exception) {
        return response(HttpStatus.BAD_REQUEST, "Os dados enviados estão inválidos. Confira os campos e tente novamente.", Map.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<ApiModels.ApiErrorResponse> status(ResponseStatusException exception) {
        return response(HttpStatus.valueOf(exception.getStatusCode().value()), exception.getReason() == null ? "Não foi possível concluir a solicitação." : exception.getReason(), Map.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public org.springframework.http.ResponseEntity<ApiModels.ApiErrorResponse> credentials(BadCredentialsException exception) {
        return response(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.", Map.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public org.springframework.http.ResponseEntity<ApiModels.ApiErrorResponse> business(RegraNegocioException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public org.springframework.http.ResponseEntity<ApiModels.ApiErrorResponse> notFound(RecursoNaoEncontradoException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<ApiModels.ApiErrorResponse> unexpected(Exception exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível concluir o cadastro agora. Tente novamente em instantes.", Map.of());
    }

    private org.springframework.http.ResponseEntity<ApiModels.ApiErrorResponse> response(HttpStatus status, String message, Map<String, String> errors) {
        return org.springframework.http.ResponseEntity.status(status).body(new ApiModels.ApiErrorResponse(message, status.value(), errors));
    }
}
