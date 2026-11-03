package br.com.murilo.bellatrama.dominio.foto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FotoRequest(
        @NotBlank(message = "A URL não pode ser nula ou em branco.")
        @Size(max = 2048, message = "A URL deve ter no máximo 2048 caracteres.")
        String url,

        @NotNull(message = "O campo principal é obrigatório.")
        Boolean principal
) {
}
