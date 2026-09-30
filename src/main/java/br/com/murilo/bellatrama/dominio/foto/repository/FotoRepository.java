package br.com.murilo.bellatrama.dominio.foto.repository;

import br.com.murilo.bellatrama.dominio.foto.model.FotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FotoRepository extends JpaRepository<FotoEntity, UUID> {

    Optional<FotoEntity> findByProdutoIdAndPrincipalTrue(UUID produtoId);

    List<FotoEntity> findByProdutoId(UUID produtoId);

    Optional<FotoEntity> findByIdAndProdutoId (UUID fotoId, UUID produtoId);

}
