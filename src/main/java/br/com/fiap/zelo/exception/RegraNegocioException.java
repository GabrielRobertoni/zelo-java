package br.com.fiap.zelo.exception;

/** Lancada quando uma operacao viola uma regra de negocio (ex.: transicao de status invalida). */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
