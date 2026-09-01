package br.com.murilo.bellatrama.dominio.produto.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponse(
        UUID id,
        String nome,
        BigDecimal preco,
        Integer estoque,
        String descricao,
        String cor,
        String material
) {
}
