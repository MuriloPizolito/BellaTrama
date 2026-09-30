package br.com.murilo.bellatrama.dominio.foto.service;

import br.com.murilo.bellatrama.dominio.foto.dto.FotoRequest;
import br.com.murilo.bellatrama.dominio.foto.dto.FotoResponse;
import br.com.murilo.bellatrama.dominio.foto.exception.FotoNaoEncontradaException;
import br.com.murilo.bellatrama.dominio.foto.model.FotoEntity;
import br.com.murilo.bellatrama.dominio.foto.repository.FotoRepository;
import br.com.murilo.bellatrama.dominio.produto.exception.ProdutoNaoEncontradoException;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.dominio.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FotoService {

    private final FotoRepository fotoRepository;
    private final ProdutoRepository produtoRepository;

    public FotoResponse cadastrar(UUID produtoId, FotoRequest dto) {
        ProdutoEntity produtoEntity = produtoRepository.findById(produtoId).orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado!"));

        FotoEntity fotoEntity = new FotoEntity();
        fotoEntity.setUrl(dto.url());
        if (dto.principal()) { // se principal for true
            Optional<FotoEntity> verificaPrincipal = fotoRepository.findByProdutoIdAndPrincipalTrue(produtoId);

            verificaPrincipal.ifPresent(foto -> {
                foto.setPrincipal(false);
                fotoRepository.save(foto);
            }); // Um produto pode possuir várias fotos, mas somente uma delas pode ser marcada como principal.
        }
        fotoEntity.setPrincipal(dto.principal());
        fotoEntity.setProduto(produtoEntity);
        FotoEntity fotoSalva = fotoRepository.save(fotoEntity);

        FotoResponse fotoResponse = new FotoResponse(fotoSalva.getId(), fotoSalva.getUrl(), fotoSalva.getPrincipal());

        return fotoResponse;
    }

    public List<FotoResponse> listarFotos(UUID produtoId) {
        ProdutoEntity produtoEntity = produtoRepository.findById(produtoId).orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado!"));

        List<FotoEntity> listaFotos = fotoRepository.findByProdutoId(produtoId);

        List<FotoResponse> fotoResponse = listaFotos.stream()
                .map(fotoEntity -> new FotoResponse(
                        fotoEntity.getId(),
                        fotoEntity.getUrl(),
                        fotoEntity.getPrincipal()
                )).toList();

        return fotoResponse;
    }

    public FotoResponse buscarPorId(UUID produtoId, UUID fotoId) {
        FotoEntity fotoEntity = fotoRepository.findByIdAndProdutoId(fotoId, produtoId).orElseThrow(() -> new FotoNaoEncontradaException("Foto não encontrada!"));

        FotoResponse fotoResponse = new FotoResponse(fotoEntity.getId(), fotoEntity.getUrl(), fotoEntity.getPrincipal());

        return fotoResponse;
    }

}
