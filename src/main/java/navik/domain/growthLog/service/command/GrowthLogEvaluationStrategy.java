package navik.domain.growthLog.service.command;

import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;

public interface GrowthLogEvaluationStrategy {
	GrowthLogResponseDTO.CreateResult create(Long userId, GrowthLogRequestDTO.CreateUserInput req);

	GrowthLogResponseDTO.RetryResult retry(Long userId, Long growthLogId);
}