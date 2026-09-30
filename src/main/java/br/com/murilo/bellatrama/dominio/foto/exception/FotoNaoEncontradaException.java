package br.com.murilo.bellatrama.dominio.foto.exception;

public class FotoNaoEncontradaException extends RuntimeException {
    public FotoNaoEncontradaException(String message) {
        super(message);
    }
}
