package br.com.murilo.bellatrama.dominio.produto.service;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoRequest;
import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoResponse;
import br.com.murilo.bellatrama.dominio.produto.exception.ProdutoNaoEncontradoException;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.dominio.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoResponse cadastrar(ProdutoRequest dto) {
        ProdutoEntity produtoEntity = new ProdutoEntity();
        produtoEntity.setNome(dto.nome());
        produtoEntity.setPreco(dto.preco());
        produtoEntity.setEstoque(dto.estoque());
        produtoEntity.setDescricao(dto.descricao());
        produtoEntity.setCor(dto.cor());
        produtoEntity.setMaterial(dto.material());
        produtoEntity.setAtivo(true);

        ProdutoEntity produtoSalvo = repository.save(produtoEntity);

        return mapearEntityToResponse(produtoSalvo);
    }

    public List<ProdutoResponse> listarProdutos() {
        List<ProdutoEntity> produtos = repository.findByAtivoTrue();

        List<ProdutoResponse> responses = produtos.stream()
                .map(this::mapearEntityToResponse).toList();

        return responses;
    }

    public ProdutoResponse buscarPorId(UUID id) {
        ProdutoEntity produtoEntity = repository.findByIdAndAtivoTrue(id).orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado!"));

        return mapearEntityToResponse(produtoEntity);
    }

    public void atualizar(UUID id, ProdutoRequest produtoRequest) {
        ProdutoEntity produtoEntity = repository.findById(id).orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado!"));
        produtoEntity.setNome(produtoRequest.nome());
        produtoEntity.setPreco(produtoRequest.preco());
        produtoEntity.setEstoque(produtoRequest.estoque());
        produtoEntity.setDescricao(produtoRequest.descricao());
        produtoEntity.setCor(produtoRequest.cor());
        produtoEntity.setMaterial(produtoRequest.material());

        repository.save(produtoEntity);
    }

    public void desativarProduto(UUID id) {
        ProdutoEntity produtoEntity = repository.findById(id).orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado!"));
        produtoEntity.setAtivo(false);

        repository.save(produtoEntity);
    }

    private ProdutoResponse mapearEntityToResponse(ProdutoEntity produtoEntity) {
        return new ProdutoResponse(
                produtoEntity.getId(),
                produtoEntity.getNome(),
                produtoEntity.getPreco(),
                produtoEntity.getEstoque(),
                produtoEntity.getDescricao(),
                produtoEntity.getCor(),
                produtoEntity.getMaterial()
        );
    }

}
