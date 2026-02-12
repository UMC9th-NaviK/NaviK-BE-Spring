package navik.domain.recruitment.repository.position.positionKpi;

import java.util.List;
import java.util.Map;

import navik.domain.recruitment.entity.PositionKpi;

public interface PositionKpiCustomRepository {
	void batchSaveAll(List<PositionKpi> positionKpis);

	Map<Long, List<String>> findPositionKpiMapByPositionIds(List<Long> positionIds);
}
