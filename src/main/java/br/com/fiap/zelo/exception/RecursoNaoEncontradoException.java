package br.com.fiap.zelo.exception;

/** Lancada quando um recurso (pet, triagem, alerta, clinica...) nao e encontrado ou nao pertence ao usuario logado. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
