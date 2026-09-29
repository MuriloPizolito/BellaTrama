package br.com.murilo.bellatrama.dominio.foto.model;

import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tb_foto", schema = "public")
@Getter
@Setter
public class FotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "url")
    private String url;

    @Column(name = "principal")
    private Boolean principal;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private ProdutoEntity produto;

}
