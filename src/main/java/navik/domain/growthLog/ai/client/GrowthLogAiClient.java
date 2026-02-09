package navik.domain.growthLog.ai.client;

import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;

public interface GrowthLogAiClient {

	GrowthLogAiResponseDTO.GrowthLogEvaluationResult evaluateUserInput(
		Long userId,
		Long jobId,
		Integer levelValue,
		GrowthLogAiRequestDTO.GrowthLogEvaluationContext context);
}
