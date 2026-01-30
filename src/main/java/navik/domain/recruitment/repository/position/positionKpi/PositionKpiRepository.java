package navik.domain.recruitment.repository.position.positionKpi;

import org.springframework.data.jpa.repository.JpaRepository;

import navik.domain.recruitment.entity.PositionKpi;

public interface PositionKpiRepository extends JpaRepository<PositionKpi, Long>, PositionKpiCustomRepository {
}
