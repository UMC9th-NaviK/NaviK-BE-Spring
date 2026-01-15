package navik.domain.growthLog.dto.res;

import java.time.LocalDateTime;
import java.util.List;

import navik.domain.growthLog.enums.GrowthType;

public class GrowthLogResponseDTO {

	public record Id(Long growthLogId) {
	}

	public record Detail(
		Long growthLogId,
		Long kpiCardId,
		GrowthType type,
		String title,
		String content,
		Integer score,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
	}

	public record ListItem(
		Long growthLogId,
		GrowthType type,
		String title,
		Integer score,
		LocalDateTime createdAt
	) {
	}

	public record Slice(
		List<ListItem> items,
		int page,
		int size,
		boolean hasNext
	) {
	}
}
