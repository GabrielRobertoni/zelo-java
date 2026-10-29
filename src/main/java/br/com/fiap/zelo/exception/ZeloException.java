package br.com.fiap.zelo.exception;

/** Base das exceções de negócio: a mensagem é sempre segura para exibir ao usuário. */
public abstract class ZeloException extends RuntimeException {
    protected ZeloException(String mensagem) {
        super(mensagem);
    }
}
