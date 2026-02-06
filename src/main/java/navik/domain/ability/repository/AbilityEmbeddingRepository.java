package navik.domain.ability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import navik.domain.ability.entity.AbilityEmbedding;

@Repository
public interface AbilityEmbeddingRepository extends JpaRepository<AbilityEmbedding, Long> {
}
