package br.com.murilo.bellatrama.dominio.foto.service;

import br.com.murilo.bellatrama.dominio.foto.dto.FotoRequest;
import br.com.murilo.bellatrama.dominio.foto.model.FotoEntity;
import br.com.murilo.bellatrama.dominio.foto.repository.FotoRepository;
import br.com.murilo.bellatrama.dominio.produto.exception.ProdutoNaoEncontradoException;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.dominio.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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

    private ProdutoEntity criarProduto() {
        ProdutoEntity produto = new ProdutoEntity();
        UUID produtoId = UUID.randomUUID();
        produto.setId(produtoId);

        return produto;
    }

}