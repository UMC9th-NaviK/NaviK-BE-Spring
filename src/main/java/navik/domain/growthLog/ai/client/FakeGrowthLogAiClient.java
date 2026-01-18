package navik.domain.growthLog.ai.client;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;

@Component
@Profile({"local", "dev", "test"})
public class FakeGrowthLogAiClient implements GrowthLogAiClient {

	@Override
	public GrowthLogAiResponseDTO.GrowthLogEvaluationResult evaluateUserInput(
		Long userId,
		GrowthLogAiRequestDTO.GrowthLogEvaluationContext context
	) {
		// 임시 응답
		return new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
			"임시 평가",
			"AI 서버 연동 전 더미 응답입니다.",
			List.of()
		);
	}
}