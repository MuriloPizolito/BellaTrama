package br.com.murilo.bellatrama.dominio.produto.repository;

import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, UUID> {
}
