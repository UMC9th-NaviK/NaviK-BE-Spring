package navik.domain.growthLog.ai;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;

@Component
@Profile({"prod"})
public class AiServerGrowthLogAiClient implements GrowthLogAiClient {

	@Override
	public GrowthLogAiResponseDTO.GrowthLogEvaluationResult evaluateUserInput(Long userId,
		GrowthLogAiRequestDTO.GrowthLogEvaluationContext context) {
		throw new UnsupportedOperationException("AI 서버 연동 구현이 필요합니다.");
	}

}
