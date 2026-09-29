package br.com.murilo.bellatrama.dominio.foto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FotoRequest(
        @NotBlank(message = "A URL não pode ser nula ou em branco.")
        String url,

        @NotNull(message = "O campo principal é obrigatório.")
        Boolean principal
) {
}
