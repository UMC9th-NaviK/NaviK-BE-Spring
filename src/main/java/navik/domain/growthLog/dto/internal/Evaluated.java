package navik.domain.growthLog.dto.internal;

import java.util.List;

import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;

public record Evaluated(
	GrowthLogAiResponseDTO.GrowthLogEvaluationResult normalized,
	List<GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta> kpis,
	int totalDelta,
	List<GrowthLogAiResponseDTO.GrowthLogEvaluationResult.AbilityResult> abilities
) {
}
