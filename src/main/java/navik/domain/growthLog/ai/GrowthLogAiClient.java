package navik.domain.growthLog.ai;

import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;

public interface GrowthLogAiClient {

	GrowthLogAiResponseDTO.GrowthLogEvaluationResult evaluateUserInput(Long userId,
		GrowthLogAiRequestDTO.GrowthLogEvaluationContext context);
}
