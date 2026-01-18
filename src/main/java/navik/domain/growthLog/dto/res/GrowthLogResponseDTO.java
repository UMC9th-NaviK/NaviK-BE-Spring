package navik.domain.growthLog.dto.res;

import java.time.LocalDateTime;

import navik.domain.growthLog.enums.GrowthLogStatus;
import navik.domain.growthLog.enums.GrowthType;

public class GrowthLogResponseDTO {

	public record Id(Long growthLogId) {
	}

	public record Detail(
		Long growthLogId,
		GrowthType type,
		String title,
		String content,
		Integer totalDelta,
		GrowthLogStatus status,
		java.time.LocalDateTime createdAt,
		java.util.List<KpiLinkItem> kpiLinks
	) {
	}

	public record ListItem(
		Long growthLogId,
		String title,
		String content,
		Integer totalDelta,
		LocalDateTime createdAt
	) {
	}

	public record Point(
		String period,
		int sumScore,
		int cumulativeScore
	) {
	}

	public record RetryResult(
		Long growthLogId,
		GrowthLogStatus status
	) {
	}

	public record KpiLinkItem(
		Long kpiCardId,
		String kpiCardName,
		Integer delta
	) {
	}

}
