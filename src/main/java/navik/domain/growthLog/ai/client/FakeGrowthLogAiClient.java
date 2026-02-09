package navik.domain.growthLog.ai.client;

import static navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.*;
import static navik.domain.growthLog.dto.res.GrowthLogAiResponseDTO.GrowthLogEvaluationResult.*;

import java.util.List;
import java.util.Random;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import navik.domain.growthLog.dto.req.GrowthLogAiRequestDTO;
import navik.domain.kpi.repository.KpiCardRepository;

@Component
@Profile("prod")
@RequiredArgsConstructor
public class FakeGrowthLogAiClient implements GrowthLogAiClient {

	private final KpiCardRepository kpiCardRepository;
	private final Random random = new Random();

	@Override
	public GrowthLogEvaluationResult evaluateUserInput(
		Long userId,
		Long jobId,
		Integer levelValue,
		GrowthLogAiRequestDTO.GrowthLogEvaluationContext context
	) {
		List<Long> kpiCardIds =
			kpiCardRepository.findTop5Ids(PageRequest.of(0, 5));

		List<GrowthLogEvaluationResult.KpiDelta> kpiDeltas =
			kpiCardIds.stream()
				.map(id -> new GrowthLogEvaluationResult.KpiDelta(
					id,
					10
				))
				.toList();

		if (kpiDeltas.isEmpty() && !context.recentKpiDeltas().isEmpty()) {
			kpiDeltas = List.of(
				new GrowthLogEvaluationResult.KpiDelta(
					context.recentKpiDeltas().get(0).kpiCardId(),
					1
				)
			);
		}

		List<GrowthLogEvaluationResult.AbilityResult> abilities = generateFakeAbilities(context.newContent());

		return new GrowthLogEvaluationResult(
			"[Fake] " + truncate(context.newContent(), 20),
			truncate(context.newContent(), 50),
			kpiDeltas,
			abilities
		);
	}

	private String truncate(String s, int maxLen) {
		if (s == null)
			return "";
		return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
	}

	private List<AbilityResult> generateFakeAbilities(String content) {
		return List.of(
			new AbilityResult(
				"[Fake] " + truncate(content, 30) + " 관련 역량",
				generateFakeEmbedding()
			),
			new AbilityResult(
				"[Fake] 문제 해결 및 분석 능력",
				generateFakeEmbedding()
			)
		);
	}

	private float[] generateFakeEmbedding() {
		float[] embedding = new float[1536];
		for (int i = 0; i < 1536; i++) {
			embedding[i] = random.nextFloat() * 2 - 1;
		}
		return embedding;
	}
}