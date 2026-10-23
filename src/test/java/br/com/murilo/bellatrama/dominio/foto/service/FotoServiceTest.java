package br.com.murilo.bellatrama.dominio.foto.service;

import br.com.murilo.bellatrama.dominio.foto.dto.FotoRequest;
import br.com.murilo.bellatrama.dominio.foto.model.FotoEntity;
import br.com.murilo.bellatrama.dominio.foto.repository.FotoRepository;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.dominio.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
        ProdutoEntity produto = new ProdutoEntity();
        UUID produtoId = UUID.randomUUID();
        FotoRequest fotoRequest = new FotoRequest(URL_FOTO, false);
        produto.setId(produtoId);

        when(produtoRepository.findById(produto.getId())).thenReturn(Optional.of(produto));
        when(fotoRepository.findByProdutoIdAndPrincipalTrue(produto.getId())).thenReturn(Optional.empty());
        when(fotoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        var fotoSalva = fotoService.cadastrar(produtoId, fotoRequest);

        // then
        verify(fotoRepository).save(captor.capture());
        FotoEntity fotoCapturada = captor.getValue();

        assertThat(fotoCapturada.getPrincipal()).isTrue();
        assertThat(fotoSalva.principal()).isTrue();
        assertThat(fotoCapturada.getProduto()).isSameAs(produto);
        assertThat(fotoCapturada.getUrl()).isEqualTo(URL_FOTO);
    }


}