package navik.domain.growthLog.dto.res;

import java.util.List;

public class GrowthLogAiResponseDTO {

	public record GrowthLogEvaluationResult(
		String title,
		String content,
		List<KpiDelta> kpis,
		List<AbilityResult> abilities
	) {
		public record KpiDelta(
			Long kpiCardId,
			Integer delta
		) {
		}

		public record AbilityResult(
			String content,
			float[] embedding
		) {
		}
	}
}
