package br.com.murilo.bellatrama.dominio.produto.repository;

import br.com.murilo.bellatrama.config.BaseRepositoryTest;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.factory.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    @DisplayName("Deve persistir produto com descrição de tamanho máximo")
    void devePersistirDescricaoComTamanhoMaximo() {
        String descricao = "x".repeat(500);

        ProdutoEntity produto = TestDataFactory.criarProduto(true);
        produto.setDescricao(descricao);

        produto = produtoRepository.save(produto);
        produtoRepository.flush();
        entityManager.clear();

        ProdutoEntity produtoEncontrado =
                produtoRepository.findById(produto.getId()).orElseThrow();

        assertThat(produtoEncontrado.getDescricao()).hasSize(500);
    }

    @Test
    @DisplayName("Deve rejeitar produto com descrição maior que 500 caracteres")
    void deveFalharAoPersistirDescricaoMaiorQue500Caracteres() {
        String descricao = "x".repeat(501);

        ProdutoEntity produto = TestDataFactory.criarProduto(true);
        produto.setDescricao(descricao);

        assertThatThrownBy(() -> produtoRepository.saveAndFlush(produto)).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("character varying(500)");
    }

    @Test
    @DisplayName("Deve ignorar produtos inativos ao buscar produtos ativos")
    void deveIgnorarProdutosInativos() {
        ProdutoEntity produtoAtivo = TestDataFactory.criarProduto(true);
        produtoRepository.save(produtoAtivo);

        ProdutoEntity produtoInativo = TestDataFactory.criarProduto(false);
        produtoRepository.save(produtoInativo);

        entityManager.flush();
        entityManager.clear();

        List<ProdutoEntity> produtoRetornado = produtoRepository.findByAtivoTrue();

        assertThat(produtoRetornado).extracting(ProdutoEntity::getId)
                .containsExactly(produtoAtivo.getId());
    }

    @Test
    @DisplayName("Deve retornar produto ativo ao buscar pelo id")
    void deveRetornarProdutoAtivoBuscandoPeloId() {
        ProdutoEntity produtoAtivo = TestDataFactory.criarProduto(true);
        produtoRepository.save(produtoAtivo);

        entityManager.flush();
        entityManager.clear();

        ProdutoEntity produtoRetornado = produtoRepository.findByIdAndAtivoTrue(produtoAtivo.getId()).orElseThrow();

        assertThat(produtoRetornado.getId()).isEqualTo(produtoAtivo.getId());
        assertThat(produtoRetornado.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar produto inativo pelo id")
    void deveRetornarOptionalVazioAoBuscarProdutoInativo() {
        ProdutoEntity produtoInativo = TestDataFactory.criarProduto(false);
        produtoRepository.save(produtoInativo);

        entityManager.flush();
        entityManager.clear();

        Optional<ProdutoEntity> produtoRetornado = produtoRepository.findByIdAndAtivoTrue(produtoInativo.getId());

        assertThat(produtoRetornado).isEmpty();
    }

}