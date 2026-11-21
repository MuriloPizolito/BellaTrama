package br.com.murilo.bellatrama.factory;

import br.com.murilo.bellatrama.dominio.foto.model.FotoEntity;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static ProdutoEntity criarProduto(boolean ativo) {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Bolsa 1");
        produto.setPreco(new BigDecimal("100.00"));
        produto.setEstoque(2);
        produto.setDescricao("Bolsa");
        produto.setCor("Laranja");
        produto.setMaterial("Barbante");
        produto.setAtivo(ativo);

        return produto;
    }

    public static FotoEntity criarFoto(ProdutoEntity produto) {
        FotoEntity foto = new FotoEntity();
        foto.setUrl("https://exemplo.com/foto-1.jpg");
        foto.setPrincipal(false);
        foto.setProduto(produto);

        return foto;
    }

    public static void definirDataCadastro(EntityManager entityManager, FotoEntity foto, LocalDateTime data) {
        entityManager.createNativeQuery(
                        "UPDATE tb_foto SET data_cadastro = :data WHERE id = :id"
                )
                .setParameter("data", data)
                .setParameter("id", foto.getId())
                .executeUpdate();
    }

}
