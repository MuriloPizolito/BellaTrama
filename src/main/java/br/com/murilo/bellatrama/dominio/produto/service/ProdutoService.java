package br.com.murilo.bellatrama.dominio.produto.service;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoRequest;
import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoResponse;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import br.com.murilo.bellatrama.dominio.produto.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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


}
