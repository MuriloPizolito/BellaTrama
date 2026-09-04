package br.com.murilo.bellatrama.dominio.produto.repository;

import br.com.murilo.bellatrama.dominio.produto.dto.ProdutoResponse;
import br.com.murilo.bellatrama.dominio.produto.model.ProdutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<ProdutoEntity, UUID> {

    Optional<ProdutoEntity> findByIdAndAtivoTrue(UUID id);

    List<ProdutoEntity> findByAtivoTrue();

}
