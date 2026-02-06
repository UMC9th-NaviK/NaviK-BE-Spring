package navik.domain.growthLog.ai.client;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO;
import navik.domain.kpi.repository.KpiCardRepository;

@Component
@Profile("!prod")
@RequiredArgsConstructor
public class FakeGrowthLogAiClient implements GrowthLogAiClient {

	private final KpiCardRepository kpiCardRepository;

	@Override
	public GrowthLogAiResponseDTO.GrowthLogEvaluationResult evaluateUserInput(
		Long userId,
		GrowthLogAiRequestDTO.GrowthLogEvaluationContext context
	) {
		List<Long> kpiCardIds =
			kpiCardRepository.findTop5Ids(PageRequest.of(0, 5));

		// context에서 KPI 정보 활용해서 더미 응답 생성
		List<GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta> kpiDeltas =
			kpiCardIds.stream()
				.map(id -> new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(
					id,
					10 // 테스트용 고정 delta
				))
				.toList();

		// KPI가 없으면 빈 리스트 유지
		if (kpiDeltas.isEmpty() && !context.recentKpiDeltas().isEmpty()) {
			kpiDeltas = List.of(
				new GrowthLogAiResponseDTO.GrowthLogEvaluationResult.KpiDelta(
					context.recentKpiDeltas().get(0).kpiCardId(),
					1
				)
			);
		}

		return new GrowthLogAiResponseDTO.GrowthLogEvaluationResult(
			"[Fake] " + truncate(context.newContent(), 20),
			"AI 서버 연동 전 더미 응답입니다. 입력: " + truncate(context.newContent(), 50),
			kpiDeltas
		);
	}

	private String truncate(String s, int maxLen) {
		if (s == null)
			return "";
		return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
	}
}