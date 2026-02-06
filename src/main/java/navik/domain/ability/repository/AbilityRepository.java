package navik.domain.ability.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import navik.domain.ability.entity.Ability;

@Repository
public interface AbilityRepository extends JpaRepository<Ability, Long> {
}
