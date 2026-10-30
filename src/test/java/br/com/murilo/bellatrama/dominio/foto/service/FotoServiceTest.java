package br.com.murilo.bellatrama.dominio.foto.service;

import br.com.murilo.bellatrama.dominio.foto.dto.FotoRequest;
import br.com.murilo.bellatrama.dominio.foto.exception.FotoNaoEncontradaException;
import br.com.murilo.bellatrama.dominio.foto.exception.FotoPrincipalNaoPodeSerDesmarcadaException;
import br.com.murilo.bellatrama.dominio.foto.model.FotoEntity;
import br.com.murilo.bellatrama.dominio.foto.repository.FotoRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FotoServiceTest {

    @InjectMocks
    FotoService fotoService;

    @Mock
    FotoRepository fotoRepository;

    @Mock
    ProdutoRepository produtoRepository;

    @Captor
    ArgumentCaptor<FotoEntity> captor;

    private static final String URL_FOTO = "https://exemplo.com/foto-5.jpg";
    private static final String NOVA_URL_FOTO = "https://exemplo.com/foto-10.jpg";

    @Nested
    @DisplayName("Cadastrar")
    class Cadastrar {

        @Test
        @DisplayName("Primeira foto do produto sempre vira principal")
        void deveMarcarPrimeiraFotoComoPrincipalMesmoQuandoRequestEnviaFalse() {
            // given
            ProdutoEntity produto = criarProduto();
            FotoRequest fotoRequest = new FotoRequest(URL_FOTO, false);

            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(fotoRepository.findByProdutoIdAndPrincipalTrue(produto.getId())).thenReturn(Optional.empty());
            when(fotoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            var fotoSalva = fotoService.cadastrar(produto.getId(), fotoRequest);

            // then
            verify(fotoRepository).save(captor.capture());
            FotoEntity fotoCapturada = captor.getValue();

            assertThat(fotoCapturada.getPrincipal()).isTrue();
            assertThat(fotoSalva.principal()).isTrue();
            assertThat(fotoCapturada.getProduto()).isSameAs(produto);
            assertThat(fotoCapturada.getUrl()).isEqualTo(URL_FOTO);
        }

        @Test
        @DisplayName("Nova foto não principal não altera a principal existente")
        void naoDeveAlterarPrincipalExistenteQuandoNovaFotoNaoForPrincipal() {
            // given
            ProdutoEntity produto = criarProduto();
            FotoEntity fotoPrincipalExistente = new FotoEntity();
            fotoPrincipalExistente.setPrincipal(true);

            FotoRequest fotoRequest = new FotoRequest(URL_FOTO, false);

            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(fotoRepository.findByProdutoIdAndPrincipalTrue(produto.getId())).thenReturn(Optional.of(fotoPrincipalExistente));
            when(fotoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            fotoService.cadastrar(produto.getId(), fotoRequest);

            // then
            verify(fotoRepository, times(1)).save(captor.capture());
            FotoEntity fotoCapturada = captor.getValue();

            assertThat(fotoPrincipalExistente.getPrincipal()).isTrue();
            assertThat(fotoCapturada.getPrincipal()).isFalse();
        }

        @Test
        @DisplayName("Nova foto principal deve alterar a principal existente para não principal")
        void deveAlterarPrincipalExistenteQuandoNovaFotoForPrincipal() {
            // given
            ProdutoEntity produto = criarProduto();
            FotoEntity fotoExistente = new FotoEntity();
            fotoExistente.setPrincipal(true);

            FotoRequest fotoRequest = new FotoRequest(URL_FOTO, true);

            when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
            when(fotoRepository.findByProdutoIdAndPrincipalTrue(produto.getId())).thenReturn(Optional.of(fotoExistente));
            when(fotoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // when
            fotoService.cadastrar(produto.getId(), fotoRequest);

            // then
            verify(fotoRepository, times(2)).save(captor.capture());
            List<FotoEntity> fotosCapturadas = captor.getAllValues();

            assertThat(fotosCapturadas).filteredOn(foto -> Boolean.TRUE.equals(foto.getPrincipal())).hasSize(1);
            assertThat(fotoExistente.getPrincipal()).isFalse();
        }

        @Test
        @DisplayName("Lança exceção ao tentar cadastrar foto com produto inexistente")
        void deveLancarExcecaoAoTentarCadastrarFotoComProdutoIdInexistente() {
            // given
            UUID produtoId = UUID.randomUUID();
            FotoRequest fotoRequest = new FotoRequest(URL_FOTO, true);

            when(produtoRepository.findById(produtoId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> fotoService.cadastrar(produtoId, fotoRequest)).isInstanceOf(ProdutoNaoEncontradoException.class);

            // then
            verifyNoInteractions(fotoRepository);
        }

    }

    @Nested
    @DisplayName("Atualizar")
    class Atualizar {

        @Test
        @DisplayName("Atualiza a url da foto mantendo foto como não principal")
        void deveAtualizarUrlMantendoFotoNaoPrincipal() {
            // given
            FotoEntity fotoASerAlterada = criarFoto(false);
            UUID produtoId = UUID.randomUUID();

            FotoRequest fotoRequest = new FotoRequest(NOVA_URL_FOTO, false);

            when(fotoRepository.findByIdAndProdutoId(fotoASerAlterada.getId(), produtoId)).thenReturn(Optional.of(fotoASerAlterada));

            // when
            fotoService.atualizar(produtoId, fotoASerAlterada.getId(), fotoRequest);

            // then
            verify(fotoRepository).save(fotoASerAlterada);
            verifyNoMoreInteractions(fotoRepository);

            assertThat(fotoASerAlterada.getPrincipal()).isFalse();
            assertThat(fotoASerAlterada.getUrl()).isEqualTo(NOVA_URL_FOTO);
        }

        @Test
        @DisplayName("Atualiza a url da foto mantendo foto como principal")
        void deveAtualizarUrlMantendoFotoComoPrincipal() {
            // given
            UUID produtoId = UUID.randomUUID();
            FotoEntity fotoASerAlterada = criarFoto(true);

            FotoRequest fotoRequest = new FotoRequest(NOVA_URL_FOTO, true);

            when(fotoRepository.findByIdAndProdutoId(fotoASerAlterada.getId(), produtoId)).thenReturn(Optional.of(fotoASerAlterada));
            when(fotoRepository.findByProdutoIdAndPrincipalTrue(produtoId)).thenReturn(Optional.of(fotoASerAlterada));

            // when
            fotoService.atualizar(produtoId, fotoASerAlterada.getId(), fotoRequest);

            // then
            verify(fotoRepository, times(1)).save(fotoASerAlterada);
            verifyNoMoreInteractions(fotoRepository);

            assertThat(fotoASerAlterada.getPrincipal()).isTrue();
            assertThat(fotoASerAlterada.getUrl()).isEqualTo(NOVA_URL_FOTO);
        }

        @Test
        @DisplayName("Desmarca a foto principal atual quando outra foto é promovida a principal")
        void deveDesmarcarPrincipalAtualQuandoOutraFotoForPromovida() {
            // given
            UUID produtoId = UUID.randomUUID();
            FotoEntity fotoASerPromovida = criarFoto(false);

            FotoEntity fotoPrincipalAtual = criarFoto(true);

            FotoRequest fotoRequest = new FotoRequest(NOVA_URL_FOTO, true);

            when(fotoRepository.findByIdAndProdutoId(fotoASerPromovida.getId(), produtoId)).thenReturn(Optional.of(fotoASerPromovida));
            when(fotoRepository.findByProdutoIdAndPrincipalTrue(produtoId)).thenReturn(Optional.of(fotoPrincipalAtual));

            // when
            fotoService.atualizar(produtoId, fotoASerPromovida.getId(), fotoRequest);

            // then
            verify(fotoRepository).save(fotoASerPromovida);
            verify(fotoRepository).save(fotoPrincipalAtual);
            verifyNoMoreInteractions(fotoRepository);

            assertThat(fotoASerPromovida.getPrincipal()).isTrue();
            assertThat(fotoASerPromovida.getUrl()).isEqualTo(NOVA_URL_FOTO);
            assertThat(fotoPrincipalAtual.getPrincipal()).isFalse();
        }

        @Test
        @DisplayName("Lança exceção ao tentar desmarcar foto principal para não principal")
        void deveLancarExcecaoAoTentarDesmarcarFotoPrincipal() {
            // given
            UUID produtoId = UUID.randomUUID();
            FotoEntity fotoPrincipal = criarFoto(true);
            fotoPrincipal.setUrl(URL_FOTO);

            FotoRequest fotoRequest = new FotoRequest(NOVA_URL_FOTO, false);

            when(fotoRepository.findByIdAndProdutoId(fotoPrincipal.getId(), produtoId)).thenReturn(Optional.of(fotoPrincipal));

            // when
            assertThatThrownBy(() -> fotoService.atualizar(produtoId, fotoPrincipal.getId(), fotoRequest)).isInstanceOf(FotoPrincipalNaoPodeSerDesmarcadaException.class);

            // then
            verify(fotoRepository, never()).save(any());

            assertThat(fotoPrincipal.getPrincipal()).isTrue();
            assertThat(fotoPrincipal.getUrl()).isEqualTo(URL_FOTO);
        }

        @Test
        @DisplayName("Lança exceção ao tentar atualizar foto inexistente")
        void deveLancarExcecaoAoTentarAtualizarFotoInexistente() {
            // given
            UUID produtoId = UUID.randomUUID();
            UUID fotoIdInexistente = UUID.randomUUID();

            FotoRequest fotoRequest = new FotoRequest(NOVA_URL_FOTO, false);

            when(fotoRepository.findByIdAndProdutoId(fotoIdInexistente, produtoId)).thenReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> fotoService.atualizar(produtoId, fotoIdInexistente, fotoRequest)).isInstanceOf(FotoNaoEncontradaException.class);

            // then
            verifyNoMoreInteractions(fotoRepository);
        }

    }

    @Nested
    @DisplayName("Deletar")
    class Deletar {

        @Test
        @DisplayName("Deleta foto não principal sem promover nenhuma outra")
        void naoDevePromoverNenhumaFotoAoDeletarFotoNaoPrincipal() {
            // given
            FotoEntity fotoDeletada = criarFoto(false);
            UUID produtoId = UUID.randomUUID();

            when(fotoRepository.findByIdAndProdutoId(fotoDeletada.getId(), produtoId)).thenReturn(Optional.of(fotoDeletada));

            // when
            fotoService.deletar(produtoId, fotoDeletada.getId());

            // then
            verify(fotoRepository).delete(fotoDeletada);
            verifyNoMoreInteractions(fotoRepository);
        }

    }

    private FotoEntity criarFoto(boolean principal) {
        FotoEntity foto = new FotoEntity();
        foto.setId(UUID.randomUUID());
        foto.setUrl(URL_FOTO);
        foto.setPrincipal(principal);

        return foto;
    }

    private ProdutoEntity criarProduto() {
        ProdutoEntity produto = new ProdutoEntity();
        produto.setId(UUID.randomUUID());

        return produto;
    }

}