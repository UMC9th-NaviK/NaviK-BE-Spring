package navik.domain.recruitment.repository.position.positionKpi;

import java.util.List;

import navik.domain.recruitment.entity.PositionKpi;

public interface PositionKpiCustomRepository {
	void batchSaveAll(List<PositionKpi> positionKpis);
}
