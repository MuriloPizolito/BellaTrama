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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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
        produtoRepository.save(produto);

        String url = "x".repeat(2048);

        FotoEntity foto = new FotoEntity();
        foto.setUrl(url);
        foto.setPrincipal(true);
        foto.setProduto(produto);

        fotoRepository.save(foto);
        fotoRepository.flush();
        entityManager.clear();

        FotoEntity fotoEncontrada = fotoRepository.findById(foto.getId()).orElseThrow();

        assertThat(fotoEncontrada.getUrl()).hasSize(2048);
    }

    @Test
    void deveFalharAoPersistirUrlMaiorQue2048Caracteres() {
        ProdutoEntity produto = criarProduto();
        produtoRepository.save(produto);

        String url = "x".repeat(2049);

        FotoEntity foto = new FotoEntity();
        foto.setUrl(url);
        foto.setPrincipal(true);
        foto.setProduto(produto);

        assertThatThrownBy(() -> fotoRepository.saveAndFlush(foto)).isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("character varying(2048)");
    }

    @Test
    void deveBuscarFotoSubstitutaMaisAntiga() {
        ProdutoEntity produto = criarProduto();
        produtoRepository.save(produto);

        FotoEntity foto2022 = criarFoto(produto);
        fotoRepository.save(foto2022);

        FotoEntity foto2020 = criarFoto(produto);
        fotoRepository.save(foto2020);

        FotoEntity foto2021 = criarFoto(produto);
        fotoRepository.save(foto2021);

        fotoRepository.flush();

        definirDataCadastro(foto2022, LocalDateTime.of(2022, 1, 1, 0, 0));
        definirDataCadastro(foto2020, LocalDateTime.of(2020, 1, 1, 0, 0));
        definirDataCadastro(foto2021, LocalDateTime.of(2021, 1, 1, 0, 0));

        entityManager.flush();
        entityManager.clear();

        FotoEntity fotoEncontrada = fotoRepository.findFirstByProdutoIdAndIdNotOrderByDataCadastroAscIdAsc(produto.getId(), foto2020.getId()).orElseThrow();

        assertThat(fotoEncontrada.getId()).isEqualTo(foto2021.getId());
    }

    @Test
    void deveIgnorarFotoDeOutroProdutoAoBuscarSubstituta() {
        ProdutoEntity produtoComFotoMaisAntiga = criarProduto();
        produtoRepository.save(produtoComFotoMaisAntiga);

        FotoEntity foto2010 = criarFoto(produtoComFotoMaisAntiga);
        fotoRepository.save(foto2010);

        ProdutoEntity produto = criarProduto();
        produtoRepository.save(produto);

        FotoEntity foto2022 = criarFoto(produto);
        fotoRepository.save(foto2022);

        FotoEntity foto2020 = criarFoto(produto);
        fotoRepository.save(foto2020);

        FotoEntity foto2021 = criarFoto(produto);
        fotoRepository.save(foto2021);

        fotoRepository.flush();

        definirDataCadastro(foto2010, LocalDateTime.of(2010, 1, 1, 0, 0));

        definirDataCadastro(foto2022, LocalDateTime.of(2022, 1, 1, 0, 0));
        definirDataCadastro(foto2020, LocalDateTime.of(2020, 1, 1, 0, 0));
        definirDataCadastro(foto2021, LocalDateTime.of(2021, 1, 1, 0, 0));

        entityManager.flush();
        entityManager.clear();

        FotoEntity fotoEncontrada = fotoRepository.findFirstByProdutoIdAndIdNotOrderByDataCadastroAscIdAsc(produto.getId(), foto2020.getId()).orElseThrow();

        assertThat(fotoEncontrada.getId()).isEqualTo(foto2021.getId());
        assertThat(fotoEncontrada.getProduto().getId()).isEqualTo(produto.getId());
    }

    @Test
    void deveRetornarFotoComMenorIdEmCasoDeEmpateNaData() {
        ProdutoEntity produto = criarProduto();
        produto = produtoRepository.save(produto);

        FotoEntity foto1 = criarFoto(produto);
        foto1 = fotoRepository.save(foto1);

        FotoEntity foto2 = criarFoto(produto);
        foto2 = fotoRepository.save(foto2);

        FotoEntity foto3 = criarFoto(produto);
        foto3 = fotoRepository.save(foto3);

        List<FotoEntity> fotosOrdenadas = Stream.of(foto1, foto2)
                .sorted(Comparator.comparing(foto -> foto.getId().toString()))
                .toList();

        FotoEntity fotoComMenorId = fotosOrdenadas.get(0);
        FotoEntity outraFoto = fotosOrdenadas.get(1);

        UUID menorId = fotoComMenorId.getId();

        definirDataCadastro(outraFoto, LocalDateTime.of(2022, 1, 1, 0, 0));
        definirDataCadastro(foto3, LocalDateTime.of(2023, 1, 1, 0, 0));

        entityManager.flush();
        // Atualiza a foto de menor ID por último, tentando deixá-la fisicamente depois na tabela.

        definirDataCadastro(fotoComMenorId, LocalDateTime.of(2022, 1, 1, 0, 0));

        entityManager.flush();
        entityManager.clear();

        FotoEntity fotoEncontrada = fotoRepository.findFirstByProdutoIdAndIdNotOrderByDataCadastroAscIdAsc(produto.getId(), foto3.getId()).orElseThrow();

        assertThat(fotoEncontrada.getId()).isEqualTo(menorId);
    }

    @Test
    void deveRetornarOptionalVazioQuandoExisteApenasFotoExcluida() {
        ProdutoEntity produto = criarProduto();
        produtoRepository.save(produto);

        FotoEntity fotoUnica = criarFoto(produto);
        fotoRepository.save(fotoUnica);

        fotoRepository.flush();

        definirDataCadastro(fotoUnica, LocalDateTime.of(2022, 1, 1, 0, 0));

        entityManager.flush();
        entityManager.clear();

        Optional<FotoEntity> fotoEncontrada = fotoRepository.findFirstByProdutoIdAndIdNotOrderByDataCadastroAscIdAsc(produto.getId(), fotoUnica.getId());

        assertThat(fotoEncontrada).isEmpty();
    }

    @Test
    void deveRetornarOptionalVazioQuandoProdutoNaoCorrespondeAoIdDaFoto() {
        ProdutoEntity produtoSemFoto = criarProduto();
        produtoRepository.save(produtoSemFoto);

        ProdutoEntity produto1 = criarProduto();
        produtoRepository.save(produto1);

        FotoEntity fotoProduto1 = criarFoto(produto1);
        fotoRepository.save(fotoProduto1);

        entityManager.flush();
        entityManager.clear();

        Optional<FotoEntity> fotoEncontrada = fotoRepository.findByIdAndProdutoId(fotoProduto1.getId(), produtoSemFoto.getId());

        assertThat(fotoEncontrada).isEmpty();
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

    private FotoEntity criarFoto(ProdutoEntity produto) {
        FotoEntity foto = new FotoEntity();
        foto.setUrl("https://exemplo.com/foto-1.jpg");
        foto.setPrincipal(false);
        foto.setProduto(produto);

        return foto;
    }

    private void definirDataCadastro(FotoEntity foto, LocalDateTime data) {
        entityManager.createNativeQuery(
                        "UPDATE tb_foto SET data_cadastro = :data WHERE id = :id"
                )
                .setParameter("data", data)
                .setParameter("id", foto.getId())
                .executeUpdate();
    }

}