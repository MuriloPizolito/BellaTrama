package br.com.murilo.bellatrama.dominio.foto.dto;

import java.util.UUID;

public record FotoResponse(
        UUID id,
        String url,
        Boolean principal
) {
}
