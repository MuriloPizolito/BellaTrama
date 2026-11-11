package br.com.murilo.bellatrama.dominio.produto.service;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoRequest;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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


}