package br.com.fiap.zelo.exception;

/** Recurso inexistente ou que não pertence ao usuário autenticado. */
public class RecursoNaoEncontradoException extends ZeloException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
