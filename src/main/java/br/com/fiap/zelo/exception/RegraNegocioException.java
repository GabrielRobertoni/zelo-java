package br.com.fiap.zelo.exception;

/** Operação que viola uma regra de negócio (ex.: transição de status inválida). */
public class RegraNegocioException extends ZeloException {
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
