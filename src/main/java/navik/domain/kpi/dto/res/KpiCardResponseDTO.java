package navik.domain.kpi.dto.res;

public class KpiCardResponseDTO {

	public record GridItem(
		Long kpiCardId,
		String name
	) {
	}

	public record Content(
		String title,
		String content
	) {
	}

	public record Detail(
		Long kpiCardId,
		String name,
		Content content
	) {
	}

	public record AllDetail(
		Long kpiCardId,
		String name,
		Content strong,
		Content weak
	) {
	}

}
