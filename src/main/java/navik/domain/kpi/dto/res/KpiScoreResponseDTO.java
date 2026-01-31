package navik.domain.kpi.dto.res;

import java.util.List;

public class KpiScoreResponseDTO {

	public record Initialize(

		int createdCount,
		int updatedCount,
		List<Item> items

	) {
	}

	public record Item(

		Long kpiCardId,
		Integer score,
		boolean created

	) {
	}

	public record Increment(
		Long kpiCardId,
		Integer score
	) {
	}

	public record Percentile(
		Long kpiCardId,
		Integer score,
		Integer topPercent,
		Integer bottomPercent
	) {
	}

	public record MonthlyTotalScoreChange(
		int year,
		int month,
		int currentScore,
		int previousScore,
		Double changeRate
	) {
	}

}
