package br.com.murilo.bellatrama.dominio.foto.repository;

import br.com.murilo.bellatrama.config.BaseRepositoryTest;
import br.com.murilo.bellatrama.dominio.foto.model.FotoEntity;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.dominio.produto.repository.ProdutoRepository;
import br.com.murilo.bellatrama.factory.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FotoRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    @DisplayName("Deve persistir foto com URL de tamanho máximo")
    void devePersistirUrlComTamanhoMaximo() {
        ProdutoEntity produto = TestDataFactory.criarProduto(true);
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
    @DisplayName("Deve rejeitar foto com URL maior que 2048 caracteres")
    void deveFalharAoPersistirUrlMaiorQue2048Caracteres() {
        ProdutoEntity produto = TestDataFactory.criarProduto(true);
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
    @DisplayName("Deve buscar a foto substituta mais antiga")
    void deveBuscarFotoSubstitutaMaisAntiga() {
        ProdutoEntity produto = TestDataFactory.criarProduto(true);
        produtoRepository.save(produto);

        FotoEntity foto2022 = TestDataFactory.criarFoto(produto);
        fotoRepository.save(foto2022);

        FotoEntity foto2020 = TestDataFactory.criarFoto(produto);
        fotoRepository.save(foto2020);

        FotoEntity foto2021 = TestDataFactory.criarFoto(produto);
        fotoRepository.save(foto2021);

        fotoRepository.flush();

        TestDataFactory.definirDataCadastro(entityManager, foto2022, LocalDateTime.of(2022, 1, 1, 0, 0));
        TestDataFactory.definirDataCadastro(entityManager, foto2020, LocalDateTime.of(2020, 1, 1, 0, 0));
        TestDataFactory.definirDataCadastro(entityManager, foto2021, LocalDateTime.of(2021, 1, 1, 0, 0));

        entityManager.flush();
        entityManager.clear();

        FotoEntity fotoEncontrada = fotoRepository.findFirstByProdutoIdAndIdNotOrderByDataCadastroAscIdAsc(produto.getId(), foto2020.getId()).orElseThrow();

        assertThat(fotoEncontrada.getId()).isEqualTo(foto2021.getId());
    }

    @Test
    @DisplayName("Deve ignorar fotos de outros produtos ao buscar substituta")
    void deveIgnorarFotoDeOutroProdutoAoBuscarSubstituta() {
        ProdutoEntity produtoComFotoMaisAntiga = TestDataFactory.criarProduto(true);
        produtoRepository.save(produtoComFotoMaisAntiga);

        FotoEntity foto2010 = TestDataFactory.criarFoto(produtoComFotoMaisAntiga);
        fotoRepository.save(foto2010);

        ProdutoEntity produto = TestDataFactory.criarProduto(true);
        produtoRepository.save(produto);

        FotoEntity foto2022 = TestDataFactory.criarFoto(produto);
        fotoRepository.save(foto2022);

        FotoEntity foto2020 = TestDataFactory.criarFoto(produto);
        fotoRepository.save(foto2020);

        FotoEntity foto2021 = TestDataFactory.criarFoto(produto);
        fotoRepository.save(foto2021);

        fotoRepository.flush();

        TestDataFactory.definirDataCadastro(entityManager, foto2010, LocalDateTime.of(2010, 1, 1, 0, 0));

        TestDataFactory.definirDataCadastro(entityManager, foto2022, LocalDateTime.of(2022, 1, 1, 0, 0));
        TestDataFactory.definirDataCadastro(entityManager, foto2020, LocalDateTime.of(2020, 1, 1, 0, 0));
        TestDataFactory.definirDataCadastro(entityManager, foto2021, LocalDateTime.of(2021, 1, 1, 0, 0));

        entityManager.flush();
        entityManager.clear();

        FotoEntity fotoEncontrada = fotoRepository.findFirstByProdutoIdAndIdNotOrderByDataCadastroAscIdAsc(produto.getId(), foto2020.getId()).orElseThrow();

        assertThat(fotoEncontrada.getId()).isEqualTo(foto2021.getId());
        assertThat(fotoEncontrada.getProduto().getId()).isEqualTo(produto.getId());
    }

    @Test
    @DisplayName("Deve retornar a foto com menor id em caso de empate na data")
    void deveRetornarFotoComMenorIdEmCasoDeEmpateNaData() {
        ProdutoEntity produto = TestDataFactory.criarProduto(true);
        produto = produtoRepository.save(produto);

        FotoEntity foto1 = TestDataFactory.criarFoto(produto);
        foto1 = fotoRepository.save(foto1);

        FotoEntity foto2 = TestDataFactory.criarFoto(produto);
        foto2 = fotoRepository.save(foto2);

        FotoEntity foto3 = TestDataFactory.criarFoto(produto);
        foto3 = fotoRepository.save(foto3);

        List<FotoEntity> fotosOrdenadas = Stream.of(foto1, foto2)
                .sorted(Comparator.comparing(foto -> foto.getId().toString()))
                .toList();

        FotoEntity fotoComMenorId = fotosOrdenadas.get(0);
        FotoEntity outraFoto = fotosOrdenadas.get(1);

        UUID menorId = fotoComMenorId.getId();

        TestDataFactory.definirDataCadastro(entityManager, outraFoto, LocalDateTime.of(2022, 1, 1, 0, 0));
        TestDataFactory.definirDataCadastro(entityManager, foto3, LocalDateTime.of(2023, 1, 1, 0, 0));

        entityManager.flush();

        // Atualiza a foto de menor ID por último, tentando deixá-la fisicamente depois na tabela.
        TestDataFactory.definirDataCadastro(entityManager, fotoComMenorId, LocalDateTime.of(2022, 1, 1, 0, 0));

        entityManager.flush();
        entityManager.clear();

        FotoEntity fotoEncontrada = fotoRepository.findFirstByProdutoIdAndIdNotOrderByDataCadastroAscIdAsc(produto.getId(), foto3.getId()).orElseThrow();

        assertThat(fotoEncontrada.getId()).isEqualTo(menorId);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando existe apenas a foto excluída")
    void deveRetornarOptionalVazioQuandoExisteApenasFotoExcluida() {
        ProdutoEntity produto = TestDataFactory.criarProduto(true);
        produtoRepository.save(produto);

        FotoEntity fotoUnica = TestDataFactory.criarFoto(produto);
        fotoRepository.save(fotoUnica);

        fotoRepository.flush();

        TestDataFactory.definirDataCadastro(entityManager, fotoUnica, LocalDateTime.of(2022, 1, 1, 0, 0));

        entityManager.flush();
        entityManager.clear();

        Optional<FotoEntity> fotoEncontrada = fotoRepository.findFirstByProdutoIdAndIdNotOrderByDataCadastroAscIdAsc(produto.getId(), fotoUnica.getId());

        assertThat(fotoEncontrada).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando o produto não corresponde ao produto da foto")
    void deveRetornarOptionalVazioQuandoProdutoNaoCorrespondeAoIdDaFoto() {
        ProdutoEntity produtoSemFoto = TestDataFactory.criarProduto(true);
        produtoRepository.save(produtoSemFoto);

        ProdutoEntity produto1 = TestDataFactory.criarProduto(true);
        produtoRepository.save(produto1);

        FotoEntity fotoProduto1 = TestDataFactory.criarFoto(produto1);
        fotoRepository.save(fotoProduto1);

        entityManager.flush();
        entityManager.clear();

        Optional<FotoEntity> fotoEncontrada = fotoRepository.findByIdAndProdutoId(fotoProduto1.getId(), produtoSemFoto.getId());

        assertThat(fotoEncontrada).isEmpty();
    }

}