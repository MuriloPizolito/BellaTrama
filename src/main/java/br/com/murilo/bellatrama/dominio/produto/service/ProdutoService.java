package br.com.murilo.bellatrama.dominio.produto.service;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoRequest;
import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoResponse;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.dominio.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

        ProdutoResponse responseDto = new ProdutoResponse(produtoSalvo.getId(), produtoSalvo.getNome(), produtoSalvo.getPreco(), produtoSalvo.getEstoque(), produtoSalvo.getDescricao(), produtoSalvo.getCor(), produtoSalvo.getMaterial());

        return responseDto;
    }

    public List<ProdutoResponse> listarProdutos() {
        List<ProdutoEntity> produtos = repository.findAll();

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


}
