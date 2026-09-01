package br.com.murilo.bellatrama.dominio.produto.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProdutoRequest(

        @NotBlank(message = "O nome não pode ser nulo ou em branco.")
        String nome,

        @NotNull(message = "O preço é obrigatório.")
        @Positive(message = "O preço deve ser maior que zero.")
        BigDecimal preco,

        @NotNull(message = "O estoque é obrigatório.")
        @PositiveOrZero(message = "O estoque não pode ser negativo.")
        Integer estoque,

        @NotBlank(message = "A descrição é obrigatória.")
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres.")
        String descricao,

        @Size(max = 50, message = "A cor deve ter no máximo 50 caracteres.")
        String cor,

        @Size(max = 100, message = "O material deve ter no máximo 100 caracteres.")
        String material

) {
}
