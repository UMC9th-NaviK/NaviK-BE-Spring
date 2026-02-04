package navik.domain.growthLog.dto.internal;

import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;

public record Evaluated(
	GrowthLogAiResponseDTO.GrowthLogEvaluationResult normalized,
	int totalDelta
) {
}
