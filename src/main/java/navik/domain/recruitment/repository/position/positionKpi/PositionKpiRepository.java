package navik.domain.recruitment.repository.position.positionKpi;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.recruitment.entity.Position;

public interface PositionKpiRepository extends JpaRepository<Position, Long>, PositionKpiCustomRepository {
}
