package br.com.murilo.bellatrama.exception;

import java.util.Map;

public record ErrorResponse(
        int status,
        String mensagem,
        Map<String, String> erros
) {
}
