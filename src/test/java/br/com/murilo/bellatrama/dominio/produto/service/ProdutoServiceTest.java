package br.com.murilo.bellatrama.dominio.produto.service;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoRequest;
import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoResponse;
import br.com.murilo.bellatrama.dominio.produto.exception.ProdutoNaoEncontradoException;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.dominio.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @InjectMocks
    ProdutoService produtoService;

    @Mock
    ProdutoRepository produtoRepository;

    @Captor
    ArgumentCaptor<ProdutoEntity> captor;

    @Nested
    @DisplayName("Cadastrar")
    class Cadastrar {

        @Test
        @DisplayName("Deve cadastrar produto como ativo")
        void deveCadastrarProdutoComoAtivo() {
            ProdutoRequest produtoRequest = criarRequest();

            when(produtoRepository.save(any(ProdutoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            produtoService.cadastrar(produtoRequest);

            verify(produtoRepository).save(captor.capture());

            assertThat(captor.getValue().getAtivo()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar produto cadastrado com os dados corretos")
        void deveRetornarProdutoCadastradoComDadosCorretos() {
            UUID idEsperado = UUID.randomUUID();
            ProdutoRequest produtoRequest = criarRequest();

            when(produtoRepository.save(any(ProdutoEntity.class))).thenAnswer(invocation -> {
                ProdutoEntity entity = invocation.getArgument(0);
                entity.setId(idEsperado);
                return entity;
            });

            var produtoResponse = produtoService.cadastrar(produtoRequest);

            verify(produtoRepository).save(any(ProdutoEntity.class));

            assertThat(produtoResponse.id()).isEqualTo(idEsperado);
            assertThat(produtoResponse.nome()).isEqualTo(produtoRequest.nome());
            assertThat(produtoResponse.preco()).isEqualByComparingTo(produtoRequest.preco());
            assertThat(produtoResponse.estoque()).isEqualTo(produtoRequest.estoque());
            assertThat(produtoResponse.descricao()).isEqualTo(produtoRequest.descricao());
            assertThat(produtoResponse.cor()).isEqualTo(produtoRequest.cor());
            assertThat(produtoResponse.material()).isEqualTo(produtoRequest.material());
        }

    }

    @Nested
    @DisplayName("Listar produtos")
    class ListarProdutos {

        @Test
        @DisplayName("Lista os produtos cadastrados")
        void deveListarProdutos() {
            ProdutoEntity produto1 = criarProduto("Bolsa 1", BigDecimal.valueOf(100), 10, "Bolsa Artesanal", "Laranja", "Algodão");
            ProdutoEntity produto2 = criarProduto("Bolsa 2", BigDecimal.valueOf(200), 1, "Bolsa de crochê", "Verde", "Barbante");
            ProdutoEntity produto3 = criarProduto("Bolsa 3", BigDecimal.valueOf(300), 3, "Bolsa de praia", "Preta", "Poliéster");
            List<ProdutoEntity> produtos = List.of(produto1, produto2, produto3);

            when(produtoRepository.findByAtivoTrue()).thenReturn(produtos);

            List<ProdutoResponse> produtoResponse = produtoService.listarProdutos();

            assertThat(produtoResponse).extracting(ProdutoResponse::id, ProdutoResponse::nome, ProdutoResponse::preco, ProdutoResponse::estoque, ProdutoResponse::descricao, ProdutoResponse::cor, ProdutoResponse::material).containsExactly(
                    tuple(produto1.getId(), produto1.getNome(), produto1.getPreco(), produto1.getEstoque(), produto1.getDescricao(), produto1.getCor(), produto1.getMaterial()),
                    tuple(produto2.getId(), produto2.getNome(), produto2.getPreco(), produto2.getEstoque(), produto2.getDescricao(), produto2.getCor(), produto2.getMaterial()),
                    tuple(produto3.getId(), produto3.getNome(), produto3.getPreco(), produto3.getEstoque(), produto3.getDescricao(), produto3.getCor(), produto3.getMaterial()));
        }

        @Test
        @DisplayName("Retorna uma lista vazia, sem produtos cadastrados")
        void deveRetornarUmaListaVazia() {
            List<ProdutoEntity> produtos = List.of();

            when(produtoRepository.findByAtivoTrue()).thenReturn(produtos);

            List<ProdutoResponse> produtoResponse = produtoService.listarProdutos();

            verify(produtoRepository).findByAtivoTrue();
            verifyNoMoreInteractions(produtoRepository);

            assertThat(produtoResponse).isEmpty();
        }

    }

    @Nested
    @DisplayName("Buscar por id")
    class BuscarPorId {

        @Test
        @DisplayName("Busca pelo id e retorna produto ativo encontrado")
        void deveBuscarERetornarProdutoAtivoEncontradoPeloId() {
            ProdutoEntity produto = criarProduto();

            when(produtoRepository.findByIdAndAtivoTrue(produto.getId())).thenReturn(Optional.of(produto));

            ProdutoResponse produtoResponse = produtoService.buscarPorId(produto.getId());

            verify(produtoRepository).findByIdAndAtivoTrue(produto.getId());
            verifyNoMoreInteractions(produtoRepository);

            assertThat(produtoResponse).extracting(ProdutoResponse::id, ProdutoResponse::nome, ProdutoResponse::preco, ProdutoResponse::estoque, ProdutoResponse::descricao, ProdutoResponse::cor, ProdutoResponse::material)
                    .containsExactly(produto.getId(), produto.getNome(), produto.getPreco(), produto.getEstoque(), produto.getDescricao(), produto.getCor(), produto.getMaterial());
        }

        @Test
        @DisplayName("Busca pelo id e não encontra produto inativo ou inexistente")
        void deveBuscarPeloIdENaoEncontrarProdutoInativoOuInexistente() {
            UUID produtoInexistente = UUID.randomUUID();

            when(produtoRepository.findByIdAndAtivoTrue(produtoInexistente)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> produtoService.buscarPorId(produtoInexistente)).isInstanceOf(ProdutoNaoEncontradoException.class);

            verify(produtoRepository, times(1)).findByIdAndAtivoTrue(produtoInexistente);
            verifyNoMoreInteractions(produtoRepository);
        }

    }

    private ProdutoRequest criarRequest() {
        return new ProdutoRequest(
                "Bolsa",
                new BigDecimal("299.90"),
                10,
                "Bolsa artesanal",
                "Laranja",
                "Algodão"
        );
    }

    private ProdutoEntity criarProduto(String nome, BigDecimal preco, Integer estoque, String descricao, String cor, String material) {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(UUID.randomUUID());
        produto.setNome(nome);
        produto.setPreco(preco);
        produto.setEstoque(estoque);
        produto.setDescricao(descricao);
        produto.setCor(cor);
        produto.setMaterial(material);

        return produto;
    }

    private ProdutoEntity criarProduto() {
        return criarProduto(
                "Bolsa",
                BigDecimal.valueOf(100),
                10,
                "Bolsa artesanal",
                "Laranja",
                "Algodão"
        );
    }

}