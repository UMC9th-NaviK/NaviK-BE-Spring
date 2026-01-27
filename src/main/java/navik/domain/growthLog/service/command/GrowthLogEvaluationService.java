package navik.domain.growthLog.service.command;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.req.GrowthLogRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogResponseDTO;

@Service
@RequiredArgsConstructor
public class GrowthLogEvaluationService {

	private final GrowthLogEvaluationStrategy strategy;

	public GrowthLogResponseDTO.CreateResult create(Long userId, GrowthLogRequestDTO.CreateUserInput req) {
		return strategy.create(userId, req);
	}

	public GrowthLogResponseDTO.RetryResult retry(Long userId, Long growthLogId) {
		return strategy.retry(userId, growthLogId);
	}
}
