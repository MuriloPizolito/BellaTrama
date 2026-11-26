package br.com.murilo.bellatrama.dominio.produto.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_produto", schema = "public")
@Getter
@Setter
public class ProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", length = 100, nullable = false)
    private String nome;

    @Column(name = "preco", precision = 10, scale = 2, nullable = false)
    private BigDecimal preco;

    @Column(name = "estoque", nullable = false)
    private Integer estoque;

    @Column(name = "descricao", length = 500, nullable = false)
    private String descricao;

    @Column(name = "cor", length = 50)
    private String cor;

    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

}
