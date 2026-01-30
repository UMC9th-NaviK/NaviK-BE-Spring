package navik.domain.kpi.dto.internal;

import java.util.List;

import navik.domain.growthLog.dto.internal.GrowthLogInternalCreateRequest;
import navik.domain.kpi.dto.res.KpiScoreResponseDTO;
import navik.domain.kpi.entity.KpiScore;

public record KpiScoreInitializeResult(
	int created,
	int updated,
	List<KpiScore> toCreate,
	List<KpiScoreResponseDTO.Item> resultItems,
	List<GrowthLogInternalCreateRequest.KpiDelta> kpiDeltasForLog
) {
}