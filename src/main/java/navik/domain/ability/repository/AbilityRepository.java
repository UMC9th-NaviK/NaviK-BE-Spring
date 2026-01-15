package navik.domain.ability.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.ability.entity.Ability;

public interface AbilityRepository extends JpaRepository<Ability, Long> {
}
