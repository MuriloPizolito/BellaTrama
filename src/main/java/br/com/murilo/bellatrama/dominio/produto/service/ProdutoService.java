package br.com.murilo.bellatrama.dominio.produto.service;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoRequest;
import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoResponse;
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

        ProdutoResponse responseDto = new ProdutoResponse(
                produtoSalvo.getId(),
                produtoSalvo.getNome(),
                produtoSalvo.getPreco(),
                produtoSalvo.getEstoque(),
                produtoSalvo.getDescricao(),
                produtoSalvo.getCor(),
                produtoSalvo.getMaterial());

        return responseDto;
    }

    public List<ProdutoResponse> listarProdutos() {
        List<ProdutoEntity> produtos = repository.findByAtivoTrue();

        List<ProdutoResponse> responses = produtos.stream()
                .map(produto -> new ProdutoResponse(
                        produto.getId(),
                        produto.getNome(),
                        produto.getPreco(),
                        produto.getEstoque(),
                        produto.getDescricao(),
                        produto.getCor(),
                        produto.getMaterial()
                )).toList();

        return responses;
    }

    public ProdutoResponse buscarPorId(UUID id) {
        ProdutoEntity produtoEntity = repository.findByIdAndAtivoTrue(id).orElseThrow(() -> new ProdutoNaoEncontradoException("Produto não encontrado!"));

        ProdutoResponse responseDto = new ProdutoResponse(
                produtoEntity.getId(),
                produtoEntity.getNome(),
                produtoEntity.getPreco(),
                produtoEntity.getEstoque(),
                produtoEntity.getDescricao(),
                produtoEntity.getCor(),
                produtoEntity.getMaterial());

        return responseDto;
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

}
