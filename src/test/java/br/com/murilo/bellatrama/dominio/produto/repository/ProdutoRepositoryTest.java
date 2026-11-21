package br.com.murilo.bellatrama.dominio.produto.repository;

import br.com.murilo.bellatrama.dominio.foto.repository.FotoRepository;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProdutoRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void devePersistirDescricaoComTamanhoMaximo() {
        String descricao = "x".repeat(500);

        ProdutoEntity produto = criarProduto(true);
        produto.setDescricao(descricao);

        produto = produtoRepository.save(produto);
        produtoRepository.flush();
        entityManager.clear();

        ProdutoEntity produtoEncontrado =
                produtoRepository.findById(produto.getId()).orElseThrow();

        assertThat(produtoEncontrado.getDescricao()).hasSize(500);
    }

    @Test
    void deveFalharAoPersistirDescricaoMaiorQue500Caracteres() {
        String descricao = "x".repeat(501);

        ProdutoEntity produto = criarProduto(true);
        produto.setDescricao(descricao);

        assertThatThrownBy(() -> produtoRepository.saveAndFlush(produto)).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("character varying(500)");
    }

    @Test
    void deveIgnorarProdutosInativos() {
        ProdutoEntity produtoAtivo = criarProduto(true);
        produtoRepository.save(produtoAtivo);

        ProdutoEntity produtoInativo = criarProduto(false);
        produtoRepository.save(produtoInativo);

        entityManager.flush();
        entityManager.clear();

        List<ProdutoEntity> produtoRetornado = produtoRepository.findByAtivoTrue();

        assertThat(produtoRetornado).extracting(ProdutoEntity::getId)
                .containsExactly(produtoAtivo.getId())
                .hasSize(1);
    }

    @Test
    void deveRetornarProdutoAtivoBuscandoPeloId() {
        ProdutoEntity produtoAtivo = criarProduto(true);
        produtoRepository.save(produtoAtivo);

        entityManager.flush();
        entityManager.clear();

        ProdutoEntity produtoRetornado = produtoRepository.findByIdAndAtivoTrue(produtoAtivo.getId()).orElseThrow();

        assertThat(produtoRetornado.getId()).isEqualTo(produtoAtivo.getId());
        assertThat(produtoRetornado.getAtivo()).isTrue();
    }

    @Test
    void deveRetornarOptionalVazioAoBuscarProdutoInativo() {
        ProdutoEntity produtoInativo = criarProduto(false);
        produtoRepository.save(produtoInativo);

        entityManager.flush();
        entityManager.clear();

        Optional<ProdutoEntity> produtoRetornado = produtoRepository.findByIdAndAtivoTrue(produtoInativo.getId());

        assertThat(produtoRetornado).isEmpty();
    }

    private ProdutoEntity criarProduto(boolean ativo) {
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

}