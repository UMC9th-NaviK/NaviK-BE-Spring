package navik.domain.growthLog.dto.req;

import java.time.LocalDateTime;
import java.util.List;

public class GrowthLogAiRequestDTO {

	public record GrowthLogEvaluationContext(
		String resumeText,
		List<PastGrowthLog> recentGrowthLogs,
		List<PastKpiDelta> recentKpiDeltas,
		String newTitle,
		String newContent
	) {
	}

	public record PastGrowthLog(
		Long id,
		String type,
		String title,
		String content,
		Integer totalDelta,
		LocalDateTime createdAt
	) {
	}

	public record PastKpiDelta(
		Long growthLogId,
		Long kpiCardId,
		Integer delta
	) {
	}
}
