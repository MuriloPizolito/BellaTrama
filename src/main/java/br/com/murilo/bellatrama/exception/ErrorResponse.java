package br.com.murilo.bellatrama.exception;

public record ErrorResponse(
        int status,
        String mensagem
) {
}
