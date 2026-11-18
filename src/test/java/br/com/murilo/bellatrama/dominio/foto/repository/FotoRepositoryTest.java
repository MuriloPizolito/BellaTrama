package br.com.murilo.bellatrama.dominio.foto.repository;

import br.com.murilo.bellatrama.dominio.foto.model.FotoEntity;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.dominio.produto.repository.ProdutoRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FotoRepositoryTest {

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
    void devePersistirUrlComTamanhoMaximo() {
        ProdutoEntity produto = criarProduto();
        produto = produtoRepository.save(produto);

        String url = "x".repeat(2048);

        FotoEntity foto = new FotoEntity();
        foto.setUrl(url);
        foto.setPrincipal(true);
        foto.setProduto(produto);

        foto = fotoRepository.save(foto);
        fotoRepository.flush();
        entityManager.clear();

        FotoEntity fotoEncontrada = fotoRepository.findById(foto.getId()).orElseThrow();

        assertThat(fotoEncontrada.getUrl()).hasSize(2048);
    }

    @Test
    void deveFalharAoPersistirUrlMaiorQue2048Caracteres() {
        ProdutoEntity produto = criarProduto();
        produto = produtoRepository.save(produto);

        String url = "x".repeat(2049);

        FotoEntity foto = new FotoEntity();
        foto.setUrl(url);
        foto.setPrincipal(true);
        foto.setProduto(produto);

        assertThatThrownBy(() -> fotoRepository.saveAndFlush(foto)).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("character varying(2048)");
    }

    private ProdutoEntity criarProduto() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setNome("Bolsa 1");
        produto.setPreco(BigDecimal.valueOf(100));
        produto.setEstoque(2);
        produto.setDescricao("Bolsa");
        produto.setCor("Laranja");
        produto.setMaterial("Barbante");
        produto.setAtivo(true);

        return produto;
    }

}