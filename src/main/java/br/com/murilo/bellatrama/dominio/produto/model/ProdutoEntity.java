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

    @Column(name = "nome")
    private String nome;

    @Column(name = "preco")
    private BigDecimal preco;

    @Column(name = "estoque")
    private Integer estoque;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "cor")
    private String cor;

    @Column(name = "material")
    private String material;

    @Column(name = "ativo")
    private Boolean ativo;


}
