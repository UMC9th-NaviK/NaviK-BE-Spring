package navik.domain.recruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.recruitment.entity.Position;

public interface PositionRepository extends JpaRepository<Long, Position> {
}
